import java.util.*;

public class Ch07SentinelFix {
    // sentinel 0
    static final class S0 {
        int[] t; int mask, size;
        S0(int c){ t=new int[c]; mask=c-1; }
        boolean add(int k){ int i=k&mask; while(t[i]!=0){ if(t[i]==k) return false; i=(i+1)&mask;} t[i]=k; size++; return true; }
        int size(){return size;}
    }
    // the standard fix: store key+1
    static final class S1 {
        int[] t; int mask, size;
        S1(int c){ t=new int[c]; mask=c-1; }
        boolean add(int k){ int s=k+1; int i=s&mask; while(t[i]!=0){ if(t[i]==s) return false; i=(i+1)&mask;} t[i]=s; size++; return true; }
        int size(){return size;}
    }
    static int d0(int[] a){ S0 s=new S0(1<<20); for(int v:a) s.add(v); return s.size(); }
    static int d1(int[] a){ S1 s=new S1(1<<20); for(int v:a) s.add(v); return s.size(); }

    public static void main(String[] x) {
        String[] names = {"[0,0]","[0,0,1]","[0,1]","[1,0,1]","[-1]","[-1,-1]","[-1,5]","[MAX,MAX]","[0]","[-2147483648]"};
        int[][] ins = {{0,0},{0,0,1},{0,1},{1,0,1},{-1},{-1,-1},{-1,5},
                       {Integer.MAX_VALUE,Integer.MAX_VALUE},{0},{Integer.MIN_VALUE}};
        int[] exp = {1,2,2,2,1,1,2,1,1,1};
        System.out.printf("%-16s %6s %8s %8s  %s%n","input","exp","sentinel0","key+1","verdict");
        for (int r=0;r<names.length;r++){
            int a=d0(ins[r]), b=d1(ins[r]);
            String v = (a==exp[r]?"s0:ok":"s0:BAD") + "  " + (b==exp[r]?"s1:ok":"s1:BAD");
            System.out.printf("%-16s %6d %8d %8d  %s%n", names[r], exp[r], a, b, v);
        }
    }
}
