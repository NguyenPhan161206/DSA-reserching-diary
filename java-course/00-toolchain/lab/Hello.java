// Ch01 — first program. Lab 01: what does the compiler actually produce?
//
// Run:  ./java-course/00-toolchain/run.sh java-course/00-toolchain/lab/Hello.java
// Then: javap -c -p .java-course-build/Hello.class   → read the bytecode of main()
public class Hello {
    public static void main(String[] args) {
        System.out.println("Hello, Java 21");
    }
}
