import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class Aufgabe3 {

    private String[] args;
    private int[][] matrix1 = new int[9][9];
    private int[][] matrix2 = new int[9][9];
    private int[][] combos;
    private Iterator<int[]> iterator;
    private List<Solution> solutions = new ArrayList<>();

    public static void main(String[] args) {
        new Aufgabe3(args);
    }

    public Aufgabe3(String[] _args){
        //args = _args; Removed for testing
        args = new String[1];
        args[0] = "beispiele/sudoku2.txt";
        readInput();
        printMatrix(matrix1);
        System.out.println();
        printMatrix(matrix2);
        System.out.println();
        compareAll();
        for (Solution solution : solutions) {
            System.out.println(solution.toString());
        }
    }

    private void compareAll(){
        generateCombos();
        for (int i = 0; i < 4; i++) {
            setIterator();
            while (iterator.hasNext()){
                boolean isValid = true;
                int[] m = iterator.next();
                int[][] temp = shuffleRows(matrix1, m);
                int[][] pM1 = shuffleColumns(temp, m);

                Iterator<Integer> i1 = Arrays.stream(pM1).flatMapToInt(Arrays::stream).iterator();
                Iterator<Integer> i2 = Arrays.stream(matrix2).flatMapToInt(Arrays::stream).iterator();

                Map<Integer, Integer> map1to2 = new HashMap<>();
                Map<Integer, Integer> map2to1 = new HashMap<>();

                while (i1.hasNext() && i2.hasNext()){
                    int n1 = i1.next();
                    int n2 = i2.next();
                    if ((n1 == 0) != (n2 == 0)){
                        isValid = false;
                        break;
                    }
                    if (n1 != 0){
                        if (map1to2.containsKey(n1)){
                            if (map1to2.get(n1) != n2){
                                isValid = false;
                                break;
                            }
                        }
                        if (map2to1.containsKey(n2)){
                            if (map2to1.get(n2) != n1){
                                isValid = false;
                                break;
                            }
                        }
                        else {
                            map1to2.put(n1, n2);
                            map2to1.put(n2, n1);
                        }
                    }
                }
                if (isValid){
                    solutions.add(new Solution(i, m, map1to2));
                    return; //remove for all solutions;
                }
            }
            matrix1 = rot90CW(matrix1);
        }
    }

    private int[][] shuffleRows(int[][] matrix, int[] map){
        int[][] res = new int[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            int bIndex = i / 3;
            int block = combos[map[3]][bIndex];
            int lIndex = i % 3;
            int line = combos[map[bIndex]][lIndex];
            res[3 * block + line] = matrix[i];
        }
        return res;
    }

    private int[][] shuffleColumns(int[][] matrix, int[] map){
        int[][] res = new int[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix[0].length; i++) {
            int bIndex = i / 3;
            int block = combos[map[7]][bIndex];
            int lIndex = i % 3;
            int line = combos[map[bIndex + 4]][lIndex];
            setColumn(res, 3 * block + line, getColumn(matrix, i));
        }
        return res;
    }

    private void generateCombos(){
        int n = 3;
        int[] A = IntStream.range(0, 3).toArray();
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
        combos = res.toArray(new int[0][]);
    }

    private void setIterator(){
        iterator = new Iterator<>() {
            int length = 8;
            int max = combos.length - 1;
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
    }

    private void readInput(){
        if (args.length < 1){
            System.out.println("Syntax: java Aufgabe3.java <Pfad zur Eingabedatei>");
            System.exit(0);
        }
        File inputFile = new File(args[0]);
        if (!inputFile.exists()){
            System.out.println("Datei existiert nicht.");
            System.exit(0);
        }
        try (BufferedReader br = new BufferedReader(
               new InputStreamReader(new FileInputStream(inputFile), StandardCharsets.UTF_8))){
            for (int i = 0; i < 9; i++) {
                matrix1[i] = parseToIntArray(br.readLine());
            }
            br.readLine();
            for (int i = 0; i < 9; i++) {
                matrix2[i] = parseToIntArray(br.readLine());
            }
        }
        catch (IOException e){
            System.out.println("Error while loading input file.");
            System.exit(0);
        }

    }

    private int[] parseToIntArray(String line){
        return Arrays.stream(line
                .replaceAll("[^0-9]+", "") //Remove unwanted characters (including BOM)
                .split("(?!^)")) //Split without first empty element
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    private int[] getColumn(int[][] matrix, int column){
        return Arrays.stream(matrix).mapToInt(row -> row[column]).toArray();
    }

    private void setColumn(int[][] matrix, int column, int[] line){
        for (int i = 0; i < matrix.length; i++) {
            matrix[i][column] = line[i];
        }
    }

    private int[][] rot90CW(int[][] matrix){
        int row = matrix.length;
        int columns = matrix[0].length;
        int[][] rotatedMat = new int[columns][row];
        for (int r = 0; r < row; r++) {
            for (int c = 0; c < columns; c++) {
                rotatedMat[c][row -1-r] = matrix[r][c];
            }
        }
        return rotatedMat;
    }

    private void printMatrix(int[][] matrix){
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    class Solution {

        private int rotations;
        private int[][] shuffles;
        private Map<Integer, Integer> numMap;

        public Solution(int _rotations, int[] _combo, Map<Integer, Integer> _numMap){
            this.rotations = _rotations;
            this.shuffles = new int[_combo.length][];
            for (int i = 0; i < _combo.length; i++) {
                shuffles[i] = combos[_combo[i]];
            }
            this.numMap = _numMap;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (rotations != 0)
                sb.append("rotations: " + rotations + "\n");
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (shuffles[i][j] != j){
                        sb.append("h-block" + i + " row" + j + " -> " + shuffles[i][j] + "\n");
                    }
                }
            }
            for (int i = 0; i < 3; i++) {
                if (shuffles[3][i] != i){
                    sb.append("h-block" + i + " -> " + shuffles[3][i] + "\n");
                }
            }
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (shuffles[i + 4][j] != j){
                        sb.append("v-block" + i + " column" + j + " -> " + shuffles[i + 4][j] + "\n");
                    }
                }
            }
            for (int i = 0; i < 3; i++) {
                if (shuffles[7][i] != i){
                    sb.append("v-block" + i + " -> " + shuffles[7][i] + "\n");
                }
            }
            for (Map.Entry<Integer, Integer> entry : numMap.entrySet()){
                if (entry.getKey() != entry.getValue()){
                    sb.append(entry.getKey() + " -> " + entry.getValue() + "\n");
                }
            }
            return sb.toString();
        }
    }
}
