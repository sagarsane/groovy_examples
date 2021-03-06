package groovy.examples

import groovy.transform.CompileStatic
import spock.lang.Specification

import static java.util.Calendar.DAY_OF_MONTH
import static java.util.Calendar.HOUR_OF_DAY
import static java.util.Calendar.MARCH
import static java.util.Calendar.MILLISECOND
import static java.util.Calendar.MINUTE
import static java.util.Calendar.MONTH
import static java.util.Calendar.SECOND
import static java.util.Calendar.YEAR
import static java.util.Calendar.ZONE_OFFSET

class ClassSpec extends Specification {

    def 'simple implicit typing'() {
        given:
        String str1 = 'a string'
        final str2 = 'another string'

        expect:
        // Groovy can still tell that str2 is a String even though it was not declared
        str1 instanceof String
        str2 instanceof String
        str2.class == String
    }


    def 'with'() {
        final calendar1 = new GregorianCalendar()
        calendar1.set(MONTH, MARCH)
        calendar1.set(YEAR, 2089)
        calendar1.set(DAY_OF_MONTH, 23)
        calendar1.set(HOUR_OF_DAY, 10)
        calendar1.set(MINUTE, 14)
        calendar1.set(SECOND, 38)
        calendar1.set(MILLISECOND, 456)
        calendar1.set(ZONE_OFFSET, -7)

        final calendar2 = new GregorianCalendar().with {
            set(MONTH, MARCH)
            set(YEAR, 2089)
            set(DAY_OF_MONTH, 23)
            set(HOUR_OF_DAY, 10)
            set(MINUTE, 14)
            set(SECOND, 38)
            final ms = 456
            set(MILLISECOND, ms)
            set(ZONE_OFFSET, -7)
            it
        }

        expect:
        calendar1 == calendar2
    }

    // **********************************************************************
    //
    // Multimethods / Multiple Dispatch
    //
    // **********************************************************************

    static class A {
    }

    static class B extends A {
    }

    @CompileStatic
    static class Dispatcher {
        String getValue(A a) {
            'A'
        }


        String getValue(B b) {
            'B'
        }
    }


    @CompileStatic
    static class StaticCaller extends Dispatcher {
        static String getName(A val) {
            new Dispatcher().getValue(val)
        }
    }

    static class DynamicCaller extends Dispatcher {
        static String getName(A val) {
            new Dispatcher().getValue(val)
        }
    }


    def 'multimethod dispatch'() {
        B b = new B()

        expect:
        StaticCaller.getName(b) == 'A'  // this is what Java would do
        DynamicCaller.getName(b) == 'B' // this is what you would "expect"
    }

    // **********************************************************************
    //
    // Bytecode for @CompileStatic vs not
    //
    // **********************************************************************


    @CompileStatic
    static class StaticClass {
        public static void main(String[] args) {
            final b = 'fooble'
            b.toLowerCase()
        }
        /*  javap -c
public static void main(java.lang.String[]);
  Code:
   0:  ldc #34; //String fooble
   2:  astore_1
   3:  aload_1
   4:  pop
   5:  aload_1
   6:  invokevirtual #40; //Method java/lang/String.toLowerCase:()Ljava/lang/String;
   9:  pop
   10: return
         */
    }


    static class DynamicClass {
        public static void main(String[] args) {
            final b = 'fooble'
            b.toLowerCase()
        }
        /* javap -c
public static void main(java.lang.String[]);
  Code:
   0:  invokestatic #21; //Method $getCallSiteArray:()[Lorg/codehaus/groovy/runtime/callsite/CallSite;
   3:  astore_1
   4:  ldc #33; //String fooble
   6:  astore_2
   7:  aload_2
   8:  pop
   9:  aload_1
   10: ldc	#34; //int 0
   12: aaload
   13: aload_2
   14: invokeinterface #40,  2; //InterfaceMethod org/codehaus/groovy/runtime/callsite/CallSite.call:(Ljava/lang/Object;)Ljava/lang/Object;
   19: pop
   20: return
         */
    }

}
