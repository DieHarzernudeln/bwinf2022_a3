import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class Test {
    public static void main(String[] args) {
        List<int[]> list = allP(8, 5);
        System.out.println(list.size());
        test2(3, IntStream.range(0, 3).toArray());
    }

    public static List<int[]> allP(int length, int max){
        List<int[]> ps = new ArrayList<>();
        int[] first = new int[length];
        for (int i = 0; i < length; i++) {
            first[i] = 0;
        }
        while (first[0] <= max){
            ps.add(first.clone());
            first[length - 1] += 1;
            for (int i = length - 1; i > 0 ; i--) {
                if (first[i] > max){
                    first[i] = 0;
                    first[i - 1] += 1;
                }
            }
        }
        return ps;
    }

    public static void test2(int n, int[] A){

        int[] c = new int[n];
        for (int i = 0; i < n; i++) {
            c[i] = 0;
        }

        System.out.println(Arrays.toString(A));

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
                System.out.println(Arrays.toString(A));
                c[i]++;
                i = 0;
            }
            else {
                c[i] = 0;
                i++;
            }
        }
    }
}
