package com.example.openrewrite_demo;

import org.junit.jupiter.api.Test;
import org.openrewrite.PathUtils;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import java.nio.file.Paths;

import static org.openrewrite.java.Assertions.java;
import static org.assertj.core.api.Assertions.assertThat;

class RenameMethodRecipeTest implements RewriteTest {
    // Set up the default recipe for renaming methods
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new SayHelloRecipe("com.example.openrewrite_demo.Example"));
    }


    @Test
    void renameMethod() {
        rewriteRun(
                java(
                        """
                            package com.example.openrewrite_demo;
        
                            class Example {
                            }
                        """,
                        """
                            package com.example.openrewrite_demo;
        
                            class Example {
                                public String hello() {
                                    return "Hello from com.example.openrewrite_demo.Example!";
                                }
                            }
                        """
                )
        );
    }

}