package com.example.openrewrite_demo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import org.jspecify.annotations.NonNull;
import org.openrewrite.*;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;

import java.util.Objects;

// Making your recipe immutable helps make them idempotent and eliminates categories of possible bugs.
// Configuring your recipe in this way also guarantees that basic validation of parameters will be done for you by rewrite.
// Also note: All recipes must be serializable. This is verified by RewriteTest.rewriteRun() in your tests.
@Value
public class SayHelloRecipe extends Recipe {
    @Option(displayName = "Fully Qualified Class Name",
            description = "A fully qualified class name indicating which class to add a hello() method to.",
            example = "com.example.openrewrite_dem.Example")
    @NonNull
    String fullyQualifiedClassName;

    // All recipes must be serializable. This is verified by RewriteTest.rewriteRun() in your tests.
    @JsonCreator
    public SayHelloRecipe(@NonNull @JsonProperty("fullyQualifiedClassName") String fullyQualifiedClassName) {
        this.fullyQualifiedClassName = fullyQualifiedClassName;
    }

    @Override
    public String getDisplayName() {
        return "Say Hello";
    }

    @Override
    public String getDescription() {
        return "Adds a \"hello\" method to the specified class.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        // getVisitor() should always return a new instance of the visitor to avoid any state leaking between cycles
        return new SayHelloVisitor();
    }


    @Override
    public String toString() {
        return "SayHelloRecipe{" +
                "fullyQualifiedClassName='" + fullyQualifiedClassName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SayHelloRecipe that = (SayHelloRecipe) o;
        return Objects.equals(fullyQualifiedClassName, that.fullyQualifiedClassName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fullyQualifiedClassName);
    }

    public String getFullyQualifiedClassName() {
        return fullyQualifiedClassName;
    }

    public class SayHelloVisitor extends JavaIsoVisitor<ExecutionContext> {
        private final JavaTemplate helloTemplate =
                JavaTemplate.builder( "public String hello() { return \"Hello from #{}!\"; }")
                        .build();

        @Override
        public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext executionContext) {
            // Don't make changes to classes that don't match the fully qualified name
            if (classDecl.getType() == null || !classDecl.getType().getFullyQualifiedName().equals(fullyQualifiedClassName)) {
                return classDecl;
            }

            // Check if the class already has a method named "hello".
            boolean helloMethodExists = classDecl.getBody().getStatements().stream()
                    .filter(statement -> statement instanceof J.MethodDeclaration)
                    .map(J.MethodDeclaration.class::cast)
                    .anyMatch(methodDeclaration -> methodDeclaration.getName().getSimpleName().equals("hello"));

            // If the class already has a `hello()` method, don't make any changes to it.
            if (helloMethodExists) {
                return classDecl;
            }

            // Interpolate the fullyQualifiedClassName into the template and use the resulting LST to update the class body
            classDecl = classDecl.withBody( helloTemplate.apply(new Cursor(getCursor(), classDecl.getBody()),
                    classDecl.getBody().getCoordinates().lastStatement(),
                    fullyQualifiedClassName ));

            return classDecl;
        }


    }
}