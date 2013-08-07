package console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.NumberFormat;

import sudoku.SudokuSolver;

public class SudokuConsoleView {
	
	public static int NUM_SOLUCOES_AVALIADAS = 0;
	public static int NUM_TABULEIROS_IGUAIS = 0;

	public static void main(String[] args) {
		char[][] in = readInitialValues();
		SudokuSolver solver = new SudokuSolver(in);
		
		long momentoInicio = System.currentTimeMillis();
		char[][] out = solver.resolve();
		long momentoTermino = System.currentTimeMillis();
		
		writeSolution(out);
		
		System.out.println("\nTempo decorrido: " + (momentoTermino - momentoInicio) + " ms\n");
	}

	public static void writeSolution(char[][] out) {
		int livre = 0;
		System.out.println("+-------+-------+-------+");
		for (int i = 0; i < 9; i++) {
			System.out.print("|");
			for (int j = 0; j < 9; j++) {
				System.out.print(" " + (out[i][j] == ' ' ? '-' : out[i][j]));
				if (j == 2 || j == 5)
					System.out.print(" |");
				if (out[i][j] == ' ')
					livre++;
			}
			System.out.println(" |");
			if (i == 2 || i == 5) {
				System.out.println("+-------+-------+-------+");
			}
		}
		NumberFormat format = NumberFormat.getInstance();
		format.setMinimumIntegerDigits(2);
		System.out.println("+-------+-------+-------+ " + format.format(livre) + " - " + NUM_SOLUCOES_AVALIADAS + " - " + NUM_TABULEIROS_IGUAIS);
		System.out.println();
	}

	private static char[][] readInitialValues() {
		char[][] in = new char[9][9];
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		try {
			
			for (int i = 0; i < 9; i++) {
				String inputLine = reader.readLine();
				String[] values = inputLine.split(" ");
				for (int j = 0; j < 9; j++) {
					char v = values[j].charAt(0);
					in[i][j] = v;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return in;
	}

}
