// Ch01/Ch12 — Lab 02: a stack trace is data, not noise.
//
// Run:  ./java-course/00-toolchain/run.sh java-course/00-toolchain/lab/StackTraceDemo.java
//
// The point: each frame is a live call on the JVM's thread stack. Reading the trace
// top-down means reading the call chain from inside outwards.
public class StackTraceDemo {

    static int thirdLayer(int x) {
        int[] data = null;          // deliberately null
        return data[x];             // NullPointerException is thrown HERE, not at the declaration
    }

    static int secondLayer(int x) {
        return thirdLayer(x);
    }

    static int firstLayer(int x) {
        return secondLayer(x);
    }

    public static void main(String[] args) {
        try {
            firstLayer(3);
        } catch (NullPointerException e) {
            System.out.println("caught: " + e.getClass().getName());
            System.out.println("--- frame walk ---");
            // frame 0 = where the exception was thrown, last frame = where we caught it
            for (StackTraceElement frame : e.getStackTrace()) {
                System.out.println("  at " + frame);
            }
            System.out.println("--- depth of the call chain at throw time: "
                    + e.getStackTrace().length + " frames ---");
        }
    }
}
