package se.streamsource.dci.restlet.server;

import org.junit.Test;
import org.qi4j.api.constraint.ConstraintDeclaration;
import org.qi4j.api.constraint.ConstraintViolation;
import org.qi4j.api.constraint.Constraints;
import org.qi4j.api.util.Iterables;
import org.qi4j.library.constraints.MaxLengthConstraint;
import org.qi4j.library.constraints.annotation.MaxLength;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;

/**
 * TODO
 */
public class ConstraintViolationMessagesTest
{
   @MaxLength(50)
   public String foo;

   @Range(min=10, max=30)
   public int bar;

   @Range(min=10, max=30, message="Wrong range")
   public int bar2;

   @Test
   public void testMessages() throws NoSuchFieldException, IllegalAccessException
   {
      {
         ConstraintViolation violation = new ConstraintViolation("foo", getClass().getField("foo").getAnnotation(MaxLength.class), 70);
         String message = new ConstraintViolationMessages().getMessage(violation, Locale.getDefault());
         System.out.println(message);
      }
      {
         ConstraintViolation violation = new ConstraintViolation("bar", getClass().getField("bar").getAnnotation(Range.class), 70);
         String message = new ConstraintViolationMessages().getMessage(violation, Locale.getDefault());
         System.out.println(message);
      }
      {
         ConstraintViolation violation = new ConstraintViolation("bar2", getClass().getField("bar2").getAnnotation(Range.class), 70);
         String message = new ConstraintViolationMessages().getMessage(violation, Locale.getDefault());
         System.out.println(message);
      }
   }

   @ConstraintDeclaration
   @Retention( RetentionPolicy.RUNTIME )
   @Constraints( MaxLengthConstraint.class )
   public @interface Range
   {
       String message() default "{Range.message}";
       int min();
       int max();
   }
}
