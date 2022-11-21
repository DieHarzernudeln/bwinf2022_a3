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
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Aufgabe3 {

    private final String[] args;
    private final int[][] matrix1 = new int[9][9];
    private final int[][] matrix2 = new int[9][9];
    private final int[][] combinations = {{0, 1, 2},
                                          {0, 2, 1},
                                          {1, 0, 2},
                                          {1, 2, 0},
                                          {2, 0, 1},
                                          {2, 1, 0}};
    private final List<Solution> solutions = new ArrayList<>();
    private Iterator<int[][]> iterator;

    public static void main(String[] args) {
        new Aufgabe3(args);
    }

    public Aufgabe3(String[] _args){
        args = _args;
        readInput();

        //Print original matrices
        System.out.println();
        System.out.println("Sudoku 1: ");
        printMatrix(matrix1);
        System.out.println("Sudoku 2: ");
        printMatrix(matrix2);

        compareAll();

        //Print results
        if (solutions.isEmpty()){
            System.out.println("Sudoku 2 ist keine Variante von Sudoku 1\n");
        }
        else{
            System.out.println("Sudoku 2 ist eine Variante von Sudoku 1\n");
            System.out.println("Umformungen (in der Reihenfolge durchfuehren): \n");
            for (Solution solution : solutions) {
                System.out.println(solution.toString());
            }
        }
    }

    //Compare all variations of matrix 1 with matrix 2 and save the first valid variation (if any)
    private void compareAll(){
        int[][] matrix1copy = matrix1;

        for (int i = 0; i < 2; i++) {
            setIterator();
            while (iterator.hasNext()){
                boolean isIdentical = true;
                int[][] lineShiftMap = iterator.next();
                /*
                    lineShiftMap[x] contains placements of ...
                    0: rows in row block 0
                    1: rows in row block 1
                    2: rows in row block 2
                    3: row blocks
                    4: columns in column block 0
                    5: columns in column block 1
                    6: columns in column block 2
                    7: column blocks
                */

                //Move rows and row blocks around
                int[][] rowsShuffled = new int[9][9];
                for (int j = 0; j < 9; j++) {
                    int bIndex = j / 3;
                    int block = lineShiftMap[3][bIndex];
                    int lIndex = j % 3;
                    int line = lineShiftMap[bIndex][lIndex];
                    rowsShuffled[3 * block + line] = matrix1copy[j];
                }

                //Move columns and column blocks around
                int[][] columnsShuffled = new int[9][9];
                for (int j = 0; j < 9; j++) {
                    int bIndex = j / 3;
                    int block = lineShiftMap[7][bIndex];
                    int lIndex = j % 3;
                    int line = lineShiftMap[bIndex + 4][lIndex];
                    setColumn(columnsShuffled, 3 * block + line, getColumn(rowsShuffled, j));
                }

                //Compare the variation with matrix 2
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
                    Map<Integer, Integer> tMap = map1to2
                            .entrySet()
                            .stream()
                            .filter(e -> !Objects.equals(e.getKey(), e.getValue()))
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    Map.Entry::getValue,
                                    (v1, v2) -> v1,
                                    TreeMap::new));
                    solutions.add(new Solution(i, lineShiftMap, tMap));
                    return; //remove this to get all solutions
                }
            }
            if (i <= 0){
                matrix1copy = rot90CW(matrix1copy);
            }
        }
    }

    //Iterate through combinations of combinations
    private void setIterator(){
        iterator = new Iterator<int[][]>() {
            final int length = 8;
            final int max = combinations.length - 1;
            final int[] first = new int[length];

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
                int[] indexes = first.clone();
                int[][] map = new int[indexes.length][];
                first[length - 1] += 1;
                for (int i = length - 1; i > 0; i--) {
                    if (first[i] > max) {
                        first[i] = 0;
                        first[i - 1] += 1;
                    }
                }
                for (int i = 0; i < length; i++) {
                    map[i] = combinations[indexes[i]];
                }
                return map;
            }
        };
    }

    //Read input using the passed arguments
    private void readInput(){
        if (args.length < 1){
            System.out.println("Syntax: Aufgabe3 <Pfad zur Eingabedatei>");
            System.exit(0);
        }
        File inputFile = new File(args[0]);
        if (!inputFile.exists()){
            System.out.println("Datei existiert nicht.");
            System.exit(1);
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
            System.exit(1);
        }

    }

    //Parse line of numbers to and integer array
    private int[] parseToIntArray(String line){
        return Arrays.stream(line
                .replaceAll("[^0-9]+", "") //Remove unwanted characters (including BOM)
                .split("(?!^)")) //Split without first empty element
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    //Get Column of a matrix
    private int[] getColumn(int[][] matrix, int column){
        return Arrays.stream(matrix).mapToInt(row -> row[column]).toArray();
    }

    //Set Column of a matrix
    private void setColumn(int[][] matrix, int column, int[] line){
        for (int i = 0; i < matrix.length; i++) {
            matrix[i][column] = line[i];
        }
    }

    //Rotate the give matrix by 90 degrees clockwise
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

    //Print the matrix in a format that is easy to read
    private void printMatrix(int[][] matrix){
        for (int[] ints : matrix) {
            for (int anInt : ints) {
                System.out.print(anInt + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    //Contains a valid solution
    private class Solution {

        private final int rotations;
        private final int[][] map;
        private final Map<Integer, Integer> numMap;

        public Solution(int _rotations, int[][] _map, Map<Integer, Integer> _numMap){
            this.rotations = _rotations;
            this.map = _map;
            this.numMap = _numMap;
        }

        //Format the solutions so that it's easy to read
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (rotations != 0)
                sb.append(rotations * 90).append(" Grad in Uhrzeigesinn drehen").append("\n");
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (map[i][j] != j){
                        sb.append(i + 1).append(". Zeilenblock, ").append(j + 1)
                                .append(". Zeile -> ").append(map[i][j] + 1).append(". Zeile\n");
                    }
                }
            }
            for (int i = 0; i < 3; i++) {
                if (map[3][i] != i){
                    sb.append(i + 1).append(". Zeilenblock -> ").append(map[3][i] + 1).append(". Zeilenblock\n");
                }
            }
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (map[i + 4][j] != j){
                        sb.append(i + 1).append(". Spaltenblock, ")
                                .append(j + 1).append(". Spalte -> ").append(map[i + 4][j] + 1).append(". Spalte\n");
                    }
                }
            }
            for (int i = 0; i < 3; i++) {
                if (map[7][i] != i){
                    sb.append(i + 1).append(". Spaltenblock -> ").append(map[7][i] + 1).append(". Spaltenblock\n");
                }
            }
            if (!numMap.isEmpty()){
                sb.append("Umbenennungen:\n");
                for (Map.Entry<Integer, Integer> entry : numMap.entrySet()){
                    if (!Objects.equals(entry.getKey(), entry.getValue())){
                        sb.append(entry.getKey()).append(" -> ").append(entry.getValue()).append("\n");
                    }
                }
            }

            return sb.toString();
        }
    }
}
