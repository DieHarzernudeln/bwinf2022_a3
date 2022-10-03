import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

public class Test {
    public static void main(String[] args) {
        System.out.println(Arrays.deepToString(test2(3, IntStream.range(0, 3).toArray())));
        Iterator<int[]> iterator = new Iterator<>() {
            int length = 8;
            int max = 5;
            int[] first = new int[length];
            {
                for (int i = 0; i < length; i++) {
                    first[i] = 0;
                }
            }
            @Override
            public boolean hasNext() {
                return first[0] <= max;
            }

            @Override
            public int[] next() {
                int[] ret = first.clone();
                first[length - 1] += 1;
                for (int i = length - 1; i > 0; i--) {
                    if (first[i] > max) {
                        first[i] = 0;
                        first[i - 1] += 1;
                    }
                }
                return ret;
            }
        };
        Object dummy;
        while (iterator.hasNext()){
            dummy = iterator.next();
        }
    }

    public static Iterable<int[]> allP(int length, int max){
        List<int[]> ps = new ArrayList<>();
        int[] first = new int[length];
        for (int i = 0; i < length; i++) {
            first[i] = 0;
        }
        return () -> new Iterator<>() {
            @Override
            public boolean hasNext() {
                return first[0] <= max;
            }

            @Override
            public int[] next() {
                int[] ret = first.clone();
                first[length - 1] += 1;
                for (int i = length - 1; i > 0; i--) {
                    if (first[i] > max) {
                        first[i] = 0;
                        first[i - 1] += 1;
                    }
                }
                return ret;
            }
        };
    }

    public static int[][] test2(int n, int[] A){
        List<int[]> res = new ArrayList<>();
        int[] c = new int[n];
        for (int i = 0; i < n; i++) {
            c[i] = 0;
        }
        res.add(A.clone());
        int i = 0;
        while (i < n) {
            if (c[i] < i) {
                int tmp = A[i];
                if (i % 2 == 0){
                    A[i] = A[0];
                    A[0] = tmp;
                }
                else{
                    A[i] = A[c[i]];
                    A[c[i]] = tmp;
                }
                res.add(A.clone());
                c[i]++;
                i = 0;
            }
            else {
                c[i] = 0;
                i++;
            }
        }
        return res.toArray(new int[0][]);
    }
}
