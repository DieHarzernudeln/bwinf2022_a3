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
    private Iterator<int[][]> iterator;
    private List<Solution> solutions = new ArrayList<>();

    public static void main(String[] args) {
        new Aufgabe3(args);
    }

    public Aufgabe3(String[] _args){
        //args = _args; Removed for testing
        args = new String[1];
        args[0] = "beispiele/sudoku3.txt";
        readInput();
        System.out.println("Sudoku 1: ");
        printMatrix(matrix1);
        System.out.println("Sudoku 2: ");
        printMatrix(matrix2);
        generateCombos();
        compareAll();
        printSolutions();
    }

    private void printSolutions(){
        if (solutions.isEmpty()){
            System.out.println("Sudoku 2 ist keine Variante von Sudoku 1\n");
        }
        else{
            System.out.println("Sudoku 2 ist eine Variante von Sudoku 1\n");
            System.out.println("Umformungen (in der Reihenfolge durchführen): \n");
            for (Solution solution : solutions) {
                System.out.println(solution.toString());
            }
        }
    }

    private void compareAll(){
        for (int i = 0; i < 4; i++) {
            setIterator();
            while (iterator.hasNext()){
                boolean isIdentical = true;
                int[][] map = iterator.next();
                int[][] rowsShuffled = new int[9][9];
                for (int j = 0; j < 9; j++) {
                    int bIndex = j / 3;
                    int block = map[3][bIndex];
                    int lIndex = j % 3;
                    int line = map[bIndex][lIndex];
                    rowsShuffled[3 * block + line] = matrix1[j];
                }
                int[][] columnsShuffled = new int[9][9];
                for (int j = 0; j < 9; j++) {
                    int bIndex = j / 3;
                    int block = map[7][bIndex];
                    int lIndex = j % 3;
                    int line = map[bIndex + 4][lIndex];
                    setColumn(columnsShuffled, 3 * block + line, getColumn(rowsShuffled, j));
                }

                Iterator<Integer> i1 = Arrays.stream(columnsShuffled).flatMapToInt(Arrays::stream).iterator();
                Iterator<Integer> i2 = Arrays.stream(matrix2).flatMapToInt(Arrays::stream).iterator();

                Map<Integer, Integer> map1to2 = new HashMap<>();
                Map<Integer, Integer> map2to1 = new HashMap<>();

                while (i1.hasNext() && i2.hasNext()){
                    int n1 = i1.next();
                    int n2 = i2.next();
                    if ((n1 == 0) != (n2 == 0)){
                        isIdentical = false;
                        break;
                    }
                    if (n1 != 0){
                        if (map1to2.containsKey(n1)){
                            if (map1to2.get(n1) != n2){
                                isIdentical = false;
                                break;
                            }
                        }
                        if (map2to1.containsKey(n2)){
                            if (map2to1.get(n2) != n1){
                                isIdentical = false;
                                break;
                            }
                        }
                        else {
                            map1to2.put(n1, n2);
                            map2to1.put(n2, n1);
                        }
                    }
                }
                if (isIdentical){
                    solutions.add(new Solution(i, map, map1to2));
                    return; //remove for all solutions;
                }
            }
            matrix1 = rot90CW(matrix1);
        }
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
            public int[][] next() {
                int[] ret = first.clone();
                int[][] ret2 = new int[ret.length][];
                first[length - 1] += 1;
                for (int i = length - 1; i > 0; i--) {
                    if (first[i] > max) {
                        first[i] = 0;
                        first[i - 1] += 1;
                    }
                }
                for (int i = 0; i < length; i++) {
                    ret2[i] = combos[ret[i]];
                }
                return ret2;
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
        private int[][] map;
        private Map<Integer, Integer> numMap;

        public Solution(int _rotations, int[][] _map, Map<Integer, Integer> _numMap){
            this.rotations = _rotations;
            this.map = _map;
            this.numMap = _numMap;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (rotations != 0)
                sb.append(rotations * 90 + " Grad in Uhrzeigesinn drehen" + "\n");
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (map[i][j] != j){
                        sb.append((i + 1) + ". Zeilenblock, " + (j + 1) + ". Zeile -> " + (map[i][j] + 1) + ". Zeile\n");
                    }
                }
            }
            for (int i = 0; i < 3; i++) {
                if (map[3][i] != i){
                    sb.append((i + 1) + ". Zeilenblock -> " + (map[3][i] + 1) + ". Zeilenblock\n");
                }
            }
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (map[i + 4][j] != j){
                        sb.append((i + 1) + ". Spaltenblock, " + (j + 1) + ". Spalte -> " + (map[i + 4][j] + 1) + ". Spalte\n");
                    }
                }
            }
            for (int i = 0; i < 3; i++) {
                if (map[7][i] != i){
                    sb.append((i + 1) + ". Spaltenblock -> " + (map[7][i] + 1) + ". Spaltenblock\n");
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
