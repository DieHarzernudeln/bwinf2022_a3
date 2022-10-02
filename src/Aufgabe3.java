import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Aufgabe3 {

    private String[] args;
    private int[][] matrix1 = new int[9][9];
    private int[][] matrix2 = new int[9][9];
    private List<Pair> pairs = new ArrayList<>();

    public static void main(String[] args) {
        new Aufgabe3(args);
    }

    public Aufgabe3(String[] _args){
        //args = _args; Removed for testing
        args = new String[1];
        args[0] = "beispiele/sudoku0.txt";
        readInput();
        printMatrix(matrix1);
        System.out.println();
        printMatrix(matrix2);
        System.out.println();
        printMatrix(rot90CW(matrix2));
        for (int i = 0; i < 1679616 * 4; i++) {
            matrix1 = rot90CW(matrix1);
        }
        System.out.println("end");
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

    private void createPairs(){
        int[] line1;
        int[] line2;
        int[] map;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                line1 = getRow(matrix1, i);
                line2 = getRow(matrix2, j);
                if ((map = compare(line1, line2)) != null)
                    pairs.add(new Pair(false, i, j, 0, map));
                if ((map = compare(line1, reversed(line2))) != null)
                    pairs.add(new Pair(false, i, j, 2, map));

                line1 = getRow(matrix1, i);
                line2 = getColumn(matrix2, j);
                if ((map = compare(line1, line2)) != null)
                    pairs.add(new Pair(false, i, j, 1, map));
                if ((map = compare(line1, reversed(line2))) != null)
                    pairs.add(new Pair(false, i, j, 3, map));

                line1 = getColumn(matrix1, i);
                line2 = getRow(matrix2, j);
                if ((map = compare(line1, line2)) != null)
                    pairs.add(new Pair(true, i, j, 3, map));
                if ((map = compare(line1, reversed(line2))) != null)
                    pairs.add(new Pair(true, i, j, 1, map));

                line1 = getColumn(matrix1, i);
                line2 = getColumn(matrix2, j);
                if ((map = compare(line1, line2)) != null)
                    pairs.add(new Pair(true, i, j, 0, map));
                if ((map = compare(line1, reversed(line2))) != null)
                    pairs.add(new Pair(true, i, j, 2, map));
            }
        }
    }

    private int[] compare(int[] line1, int[] line2){
        int[] pattern1 = getPattern(line1);
        int[] pattern2 = getPattern(line2);
        if (Arrays.equals(pattern1, pattern2)){
            return pattern1;
        }
        return null;
    }

    private int[] getPattern(int[] line){
        int[] pattern = new int[line.length];
        List<Integer> found = new ArrayList<>();
        for (int i = 0; i < line.length; i++) {
            if (line[i] != 0){
                if (found.contains(line[i])){
                    pattern[i] = found.indexOf(line[i]);
                }
                else{
                    found.add(line[i]);
                    pattern[i] = found.size();
                }
            }
            else{
                pattern[i] = 0;
            }
        }
        return pattern;
    }

    private int[] reversed(int[] ints){
        for (int i = 0; i < ints.length / 2; i++) {
            int temp = ints[i];
            ints[i] = ints[ints.length - i - 1];
            ints[ints.length - i - 1] = temp;
        }
        return ints;
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

    private int[] getRow(int[][] matrix, int row){
        return matrix[row];
    }

    private int[][] rot90CW(int[][] matrix){
        int row = matrix.length;
        int colums = matrix[0].length;
        int[][] rotatedMat = new int[colums][row];
        for (int r = 0; r < row; r++) {
            for (int c = 0; c < colums; c++) {
                rotatedMat[c][row -1-r] = matrix[r][c];
            }
        }
        return rotatedMat;
    }

    private int numberFilled(int[][] matrix){
        int count = 0;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (matrix[i][j] != 0)
                    count++;
            }
        }
        return count;
    }

    class Pair {
        private boolean column; //line1
        private int line1Index;
        private int line2Index;
        private int rotations;
        private int[] map;
        public Pair(boolean _column, int _line1Index, int _line2Index, int _rotations, int[] _map){
            column = _column;
            line1Index = _line1Index;
            line2Index = _line2Index;
            rotations = _rotations;
            map = _map;
        }

        @Override
        public String toString() {
            return "\nLine1 is column: " + column +
                    "\nLine1 index: " + line1Index +
                    "\nLine2 index: " + line2Index +
                    "\nRotations: " + rotations +
                    "\nMap: " + Arrays.toString(map) +
                    "\n";
        }
    }

    private void printMatrix(int[][] matrix){
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
    }
}
