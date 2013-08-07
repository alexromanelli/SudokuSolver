package sudoku;

import java.util.ArrayList;
import java.util.ListIterator;

import console.SudokuConsoleView;

/**
 * <p>
 * A classe SudokuSolver cuida de encontrar uma solução para uma configuração
 * inicial de um "tabuleiro" sudoku. Uma solução válida é tal que todos os
 * valores presentes na configuração inicial permanecem fixos em suas posições,
 * e todos os valores posicionados devem respeitar as seguintes condições, ou
 * regras:
 * </p>
 * <ol>
 * <li>Cada linha do tabuleiro deve ter todos os algarismos de 1 a 9, sendo um
 * por célula;</li>
 * <li>Cada coluna do tabuleiro deve ter todos os algarismos de 1 a 9, sendo um
 * por célula;</li>
 * <li>Cada um dos nove blocos 3x3 do tabuleiro deve ter todos os algarismos de
 * 1 a 9, sendo um por célula.</li>
 * </ol>
 * <p>
 * Uma célula é uma região do tabuleiro que pode ser preenchido com um algarismo
 * de 1 a 9. Uma linha é uma sequência de nove células dispostas cada uma à
 * direita da anterior. Uma coluna é uma sequência de nove células dispostas
 * cada uma abaixo da anterior. Um bloco 3x3 é um agrupamento de nove células,
 * como ilustrado abaixo. Nesta ilustração, os caracteres 'c' representam as
 * células do tabuleiro.
 * </p>
 * 
 * <pre>
 *  +-------+-------+-------+
 *  | c c c | c c c | c c c |
 *  | c c c | c c c | c c c |
 *  | c c c | c c c | c c c |
 *  +-------+-------+-------+
 *  | c c c | c c c | c c c |
 *  | c c c | c c c | c c c |
 *  | c c c | c c c | c c c |
 *  +-------+-------+-------+
 *  | c c c | c c c | c c c |
 *  | c c c | c c c | c c c |
 *  | c c c | c c c | c c c |
 *  +-------+-------+-------+
 * </pre>
 * 
 * @author Alexandre Romanelli
 * 
 */
public class SudokuSolver {

	// relação dos algarismos (valores aceitos para preenchimento das células)
	private static final char[] ALGARISMOS = { '1', '2', '3', '4', '5', '6',
			'7', '8', '9' };

	// especificação do caractere que representa uma célula livre
	private static final char LIVRE = ' ';

	// estrutura para armazenar os estados inválidos, ou seja, aqueles que já se
	// sabe que não levam à solução
	private static ArrayList<char[]> ESTADOS_INVALIDOS = new ArrayList<char[]>();

	// enumeração dos tipos de conjecturas que se pode fazer
	private enum TipoConjectura {
		BLOCO, LINHA, COLUNA
	}

	/**
	 * A classe interna Celula representa a estrutura necessária para armazenar
	 * os dados relevantes para a busca efetuada no tabuleiro. Basicamente,
	 * constitui-se de um caractere, que armazena o algarismo preenchido na
	 * célula, e de um vetor de valores lógicos, com um valor para cada
	 * algarismo de 1 a 9. Se o algarismo for válido para ser preenchido na
	 * célula, seu valor lógico é true. Se não for válido, o valor lógico é
	 * false.
	 * 
	 * @author Alexandre Romanelli
	 * 
	 */
	private class Celula {
		private char algarismo;
		private boolean[] validadeAlgarismo;

		public Celula(char algarismo) {
			this.algarismo = algarismo;
			validadeAlgarismo = new boolean[9];
			for (int i = 0; i < 9; i++)
				validadeAlgarismo[i] = true;
		}

		public char getAlgarismo() {
			return algarismo;
		}

		public void setAlgarismo(char alg) {
			this.algarismo = alg;
			// se o algarismo preenchido na célula representar uma posição
			// ocupada, então não há mais algarismos válidos para esta célula
			if (alg != LIVRE)
				for (int i = 0; i < 9; i++)
					validadeAlgarismo[i] = false;
		}

		public boolean getValidadeAlgarismo(char alg) {
			int indAlg = Integer.parseInt("" + alg) - 1;
			return validadeAlgarismo[indAlg];
		}

		public void setValidadeAlgarismo(char alg, boolean valido) {
			int indAlg = Integer.parseInt("" + alg) - 1;
			validadeAlgarismo[indAlg] = valido;
		}
	} // fim da declaração da classe Celula

	/**
	 * A classe Conjectura representa uma hipótese para o preenchimento de um
	 * algarismo. Esta conjectura é feita por bloco 3x3, por linha ou por
	 * coluna. Estas variações são denominadas tipos de conjectura. Cada
	 * conjectura pode possuir uma coleção de posições válidas para um
	 * algarismo. Neste caso, a hipótese deve ser considerada pela disjunção do
	 * preenchimento do algarismo em cada posição válida. Ou seja, se, por
	 * exemplo, identifica-se que o algarismo 2 na linha 4 pode estar nas coluna
	 * 4 e 5, então deve-se interpretar que o algarismo 2 está na célula de
	 * linha 4 e coluna 4, ou o algarismo 2 está na célula de linha 4 e coluna
	 * 5.
	 * 
	 * @author Alexandre Romanelli
	 * 
	 */
	private class Conjectura {
		// o algarismo considerado na conjectura
		private char algarismo;

		// o tipo de conjectura (por bloco, por linha ou por coluna)
		private TipoConjectura tipo;

		// a coleção de posições válidas para o preenchimento do algarismo
		private ArrayList<Integer> posicoesValidas;

		public Conjectura(char algarismo, TipoConjectura tipo) {
			super();
			this.algarismo = algarismo;
			this.tipo = tipo;
			this.posicoesValidas = new ArrayList<Integer>();
		}

		public char getAlgarismo() {
			return algarismo;
		}

		@SuppressWarnings("unused")
		public TipoConjectura getTipo() {
			return tipo;
		}

		public ArrayList<Integer> getPosicoesValidas() {
			return posicoesValidas;
		}

		public void addPosicaoValida(int linha, int coluna) {
			posicoesValidas.add(linha * 9 + coluna);
		}
	} // fim da declaração da classe Conjectura

	// estrutura usada para armazenar os valores da configuração inicial do jogo
	// ==> TODO Remover este campo.
	private Celula[][] quadro;

	private int numCelulasLivresInicio;

	/**
	 * Construtor da classe SudokuSolver.
	 * 
	 * @param inicio
	 *            matriz 9x9 de caracteres, que representa a configuração
	 *            inicial do tabuleiro do jogo
	 */
	public SudokuSolver(char[][] inicio) {
		quadro = new Celula[9][9];
		numCelulasLivresInicio = 0;

		// transfere os caracteres da configuração inicial para a matriz 9x9
		// "quadro", composto por objetos da classe Celula
		for (int i = 0; i < 9; i++)
			for (int j = 0; j < 9; j++) {
				char alg = inicio[i][j];
				if (alg == '-') {
					alg = LIVRE;
					numCelulasLivresInicio++;
				}
				quadro[i][j] = new Celula(alg);
			}
	}

	/**
	 * Executa a busca por uma combinação de preenchimentos de algarismos nas
	 * células livres do tabuleiro, de modo que todas as células fiquem
	 * preenchidas e que sejam respeitadas as três regras fundamentais do jogo.
	 * 
	 * @return uma matriz 9x9 de caracteres, que representa a configuração final
	 *         do tabuleiro do jogo.
	 */
	public char[][] resolve() {
		ESTADOS_INVALIDOS.clear();
		return resolve(quadro, numCelulasLivresInicio);
	}

	/**
	 * Este método implementa a busca em profundidade com backtracking, através
	 * de chamadas recursivas. Basicamente, o fluxo de ações deste método é:<br/>
	 * <ul>
	 * <li>Se o estado atual do tabuleiro for reconhecidamente inválido, aborta;
	 * </li>
	 * <li>Faz uma cópia do estado atual do tabuleiro, para preservar os valores
	 * originais, necessários em caso de backtracking com o retorno da chamada
	 * recursiva;</li>
	 * <li>Entra em um loop, que repetirá seus passos enquanto houver
	 * atualizações do estado atual. Seus passos são:
	 * <ul>
	 * <li>Define os algarismos que são válidos em cada célula;</li>
	 * <li>Se o estado atual não for válido, aborta (e registra o estado
	 * inviável);</li>
	 * <li>Identifica as conjecturas que podem ser feitas para o estado atual;</li>
	 * <li>Usa todas as conjecturas com apenas uma posição válida para algum
	 * caractere, para preencher a célula da respectiva posição;</li>
	 * <li>Remove, da lista de conjecturas, todas as que foram usadas no passo
	 * anterior;</li>
	 * <li>Se a solução tiver sido encontrada, retorná-la;
	 * </ul>
	 * </li>
	 * <li>Para cada posição válida de cada conjectura da lista de conjecturas,
	 * preencher a célula da posição com o respectivo algarismo (da conjectura),
	 * e, se ainda houver pendências, fazer uma chamada recursiva ao método,
	 * para resolver o tabuleiro após o posicionamento feito;</li>
	 * <li>Se a solução tiver sido encontrada, retorná-la;</li>
	 * <li>Se o tabuleiro recém-modificado levar a um estado inviável para
	 * resolução, fazer o backtracking, removendo o algarismo da célula da
	 * posição avaliada, e seguindo para a próxima posição válida, ou para a
	 * próxima conjectura, se as posições válidas para a conjectura em análise
	 * estiverem esgotadas.</li>
	 * <li>Se as conjecturas forem completamente analisadas, mas nenhuma levar a
	 * uma solução válida, insere o estado atual na lista de estados inválidos e
	 * aborta.</li>
	 * </ul>
	 * 
	 * @param t
	 *            o tabuleiro atual do jogo (matriz 9x9 de caracteres)
	 * @param numCelulasLivres
	 *            o número de células não preenchidas do tabuleiro do jogo
	 * @return uma matriz 9x9 de caracteres que representa a configuração final
	 *         do jogo, se for encontrada, ou null, caso contrário.
	 */
	private char[][] resolve(Celula[][] t, int numCelulasLivres) {
		if (tabInvalidoConhecido(t))
			return null;
		SudokuConsoleView.NUM_SOLUCOES_AVALIADAS++;

		Celula[][] tab = new Celula[9][9];
		for (int i = 0; i < 9; i++)
			for (int j = 0; j < 9; j++) {
				tab[i][j] = new Celula(t[i][j].getAlgarismo());
			}
		// para testes
		// ******************************************************************************
		// if (SudokuConsoleView.NUM_SOLUCOES_AVALIADAS % 10 == 0)
		// SudokuConsoleView.writeSolution(converteCelulasParaChar(tab));
		// ------------------------------------------------------------------------------------------

		ArrayList<Conjectura> conjecturas = new ArrayList<Conjectura>();
		int contAtualizacoes;
		do {
			contAtualizacoes = 0;
			defAlgarismosValidosCelulasLivres(tab);
			if (!estadoAtualViavel(tab)) {
				insereEstadoInvalido(tab);
				return null;
			}

			conjecturas.clear();
			identificaConjecturas(tab, conjecturas);
			// boolean preenchido = preencheCelulaComOpcaoUnica(tab,
			// conjecturas);
			// numCelulasLivres -= preenchido ? 1 : 0;
			ArrayList<Integer> conjOpcaoUnica = new ArrayList<Integer>();
			for (Conjectura conj : conjecturas) {
				if (conj.getPosicoesValidas().size() == 1) {
					conjOpcaoUnica.add(conjecturas.indexOf(conj));
					char alg = conj.getAlgarismo();
					int l = conj.getPosicoesValidas().get(0) / 9;
					int c = conj.getPosicoesValidas().get(0) % 9;
					if (tab[l][c].getAlgarismo() == LIVRE) {
						tab[l][c].setAlgarismo(alg);
						numCelulasLivres--;
						contAtualizacoes++;

						// para testes
						// ******************************************************************************
						SudokuConsoleView
								.writeSolution(converteCelulasParaChar(tab));
						// ------------------------------------------------------------------------------------------
					}
				} else
					break;
			}

			// remove conjecturas com opção única, que já foram usadas
			ListIterator<Integer> it = conjOpcaoUnica
					.listIterator(conjOpcaoUnica.size());
			while (it.hasPrevious())
				conjecturas.remove(it.previous());

			// se encontrou a solução... :)
			if (numCelulasLivres == 0)
				return converteCelulasParaChar(tab);
		} while (contAtualizacoes > 0); // condição do loop

		for (Conjectura conjectura : conjecturas) {
			char alg = conjectura.getAlgarismo();
			for (Integer posicaoValida : conjectura.getPosicoesValidas()) {
				int linha = posicaoValida / 9;
				int coluna = posicaoValida % 9;

				tab[linha][coluna].setAlgarismo(alg);
				numCelulasLivres--;
				if (numCelulasLivres == 0)
					return converteCelulasParaChar(tab);

				char[][] resultado = resolve(tab, numCelulasLivres);
				if (resultado != null)
					return resultado;

				tab[linha][coluna].setAlgarismo(LIVRE);
				numCelulasLivres++;
			}
		}

		insereEstadoInvalido(tab);
		return null;
	}

	private void insereEstadoInvalido(Celula[][] tab) {
		char[] estadoInvalido = new char[81]; // tabuleiro 9x9
		int ind = 0;
		for (int i = 0; i < 9; i++)
			for (int j = 0; j < 9; j++)
				estadoInvalido[ind++] = tab[i][j].getAlgarismo();
		ESTADOS_INVALIDOS.add(estadoInvalido);
	}

	private boolean tabInvalidoConhecido(Celula[][] tab) {
		char[] estadoAtual = new char[81]; // tabuleiro 9x9
		int ind = 0;
		for (int i = 0; i < 9; i++)
			for (int j = 0; j < 9; j++)
				estadoAtual[ind++] = tab[i][j].getAlgarismo();

		loopInvalidos: for (char[] inv : ESTADOS_INVALIDOS) {
			for (int i = 0; i < 81; i++)
				if (inv[i] != estadoAtual[i])
					continue loopInvalidos;
			SudokuConsoleView.NUM_TABULEIROS_IGUAIS++;
			return true;
		}

		return false;
	}

	/**
	 * Este método avalia se há pelo menos uma posição válida para cada
	 * algarismo pendente por bloco, por linha e por coluna. Se houver, então o
	 * estado atual do tabuleiro pode ser considerado viável. Se, por outro
	 * lado, há algum algarismo pendente em um bloco e sem posição válida neste,
	 * ou pendente em uma linha e sem posição válida nesta, ou pendente em uma
	 * coluna e sem posição válida nesta, o estado atual do tabuleiro é
	 * considerado inviável.
	 * 
	 * @param tab
	 * @return O valor lógico da proposição que afirma ser viável o tabuleiro
	 *         atual.
	 */
	private boolean estadoAtualViavel(Celula[][] tab) {
		boolean[] algPendente = new boolean[9];

		// avalia blocos
		for (int linhaBloco = 0; linhaBloco < 3; linhaBloco++)
			for (int colunaBloco = 0; colunaBloco < 3; colunaBloco++) {
				// inicia vetor de algarismos pendentes com true
				for (int i = 0; i < 9; i++)
					algPendente[i] = true;

				// identifica algarismos que não estão pendentes
				for (int i = linhaBloco * 3; i < linhaBloco * 3 + 3; i++)
					for (int j = colunaBloco * 3; j < colunaBloco * 3 + 3; j++)
						if (tab[i][j].getAlgarismo() != LIVRE) {
							int indAlg = Integer.parseInt(""
									+ tab[i][j].getAlgarismo()) - 1;
							algPendente[indAlg] = false;
						}

				// verifica se há posição válida para cada algarismo pendente
				for (int indAlg = 0; indAlg < 9; indAlg++)
					if (algPendente[indAlg]) {
						char alg = ALGARISMOS[indAlg];
						boolean temPosValida = false;

						loopBlocoViabilidade: for (int i = linhaBloco * 3; i < linhaBloco * 3 + 3; i++)
							for (int j = colunaBloco * 3; j < colunaBloco * 3 + 3; j++)
								if (tab[i][j].getAlgarismo() == LIVRE
										&& tab[i][j].getValidadeAlgarismo(alg)) {
									temPosValida = true;
									break loopBlocoViabilidade;
								}

						if (!temPosValida)
							return false;
					}
			}

		// avalia linhas
		for (int linha = 0; linha < 9; linha++) {
			// inicia vetor de algarismos pendentes com true
			for (int i = 0; i < 9; i++)
				algPendente[i] = true;

			// identifica algarismos que não estão pendentes
			for (int j = 0; j < 9; j++)
				if (tab[linha][j].getAlgarismo() != LIVRE) {
					int indAlg = Integer.parseInt(""
							+ tab[linha][j].getAlgarismo()) - 1;
					algPendente[indAlg] = false;
				}

			// verifica se há posição válida para cada algarismo pendente
			for (int indAlg = 0; indAlg < 9; indAlg++)
				if (algPendente[indAlg]) {
					char alg = ALGARISMOS[indAlg];
					boolean temPosValida = false;

					for (int j = 0; j < 9; j++)
						if (tab[linha][j].getAlgarismo() == LIVRE
								&& tab[linha][j].getValidadeAlgarismo(alg)) {
							temPosValida = true;
							break;
						}

					if (!temPosValida)
						return false;
				}
		}

		// avalia colunas
		for (int coluna = 0; coluna < 9; coluna++) {
			// inicia vetor de algarismos pendentes com true
			for (int i = 0; i < 9; i++)
				algPendente[i] = true;

			// identifica algarismos que não estão pendentes
			for (int i = 0; i < 9; i++)
				if (tab[i][coluna].getAlgarismo() != LIVRE) {
					int indAlg = Integer.parseInt(""
							+ tab[i][coluna].getAlgarismo()) - 1;
					algPendente[indAlg] = false;
				}

			// verifica se há posição válida para cada algarismo pendente
			for (int indAlg = 0; indAlg < 9; indAlg++)
				if (algPendente[indAlg]) {
					char alg = ALGARISMOS[indAlg];
					boolean temPosValida = false;

					for (int i = 0; i < 9; i++)
						if (tab[i][coluna].getAlgarismo() == LIVRE
								&& tab[i][coluna].getValidadeAlgarismo(alg)) {
							temPosValida = true;
							break;
						}

					if (!temPosValida)
						return false;
				}
		}

		return true;
	}

	private char[][] converteCelulasParaChar(Celula[][] tab) {
		char[][] t = new char[9][9];
		for (int i = 0; i < 9; i++)
			for (int j = 0; j < 9; j++) {
				t[i][j] = tab[i][j].getAlgarismo();
			}

		return t;
	}

	/**
	 * Este método faz a identificação de conjecturas para o preenchimento de
	 * células no tabuleiro.<br/>
	 * Uma conjectura indica, por exemplo, que o algarismo 2 pode ocupar, na
	 * quarta linha, as posições 5 e 6. Podem haver conjecturas para cada
	 * algarismo pendente por bloco, por linha ou por coluna.<br/>
	 * As conjecturas são ordenadas por quantidade de opções para posicionar um
	 * algarismo. A ordem adotada é crescente.
	 * 
	 * @param tab
	 *            o estado atual do tabuleiro
	 * @param conjecturas
	 *            a coleção que armazenará as conjecturas identificadas
	 */
	private void identificaConjecturas(Celula[][] tab,
			ArrayList<Conjectura> conjecturas) {
		for (char alg : ALGARISMOS) {
			// identifica conjecturas por bloco
			for (int bl = 0; bl < 3; bl++)
				loopColBloco: for (int bc = 0; bc < 3; bc++) {
					// verifica se o algarismo é pendente no bloco
					for (int i = bl * 3; i < bl * 3 + 3; i++)
						for (int j = bc * 3; j < bc * 3 + 3; j++)
							if (tab[i][j].getAlgarismo() == alg)
								continue loopColBloco; // se não é pendente,
														// pula para o próximo
														// bloco

					// cria conjectura para o algarismo no bloco atual
					Conjectura conj = new Conjectura(alg, TipoConjectura.BLOCO);

					// para cada célula livre válida para ser ocupada pelo
					// algarismo, insere esta possibilidade na conjectura
					for (int i = bl * 3; i < bl * 3 + 3; i++)
						for (int j = bc * 3; j < bc * 3 + 3; j++)
							if (tab[i][j].getAlgarismo() == LIVRE
									&& tab[i][j].getValidadeAlgarismo(alg))
								conj.addPosicaoValida(i, j);

					// insere conjectura na lista de conjecturas
					insereConjecturaOrdenadamente(conj, conjecturas);
				}

			// identifica conjecturas por linha
			loopLinha: for (int l = 0; l < 9; l++) {
				// verifica se o algarismo é pendente na linha
				for (int c = 0; c < 9; c++)
					if (tab[l][c].getAlgarismo() == alg)
						continue loopLinha; // se não é pendente, pula para a
											// próxima linha

				// cria conjectura para o algarismo na linha atual
				Conjectura conj = new Conjectura(alg, TipoConjectura.LINHA);

				// para cada célula livre válida para ser ocupada pelo
				// algarismo, insere esta possibilidade na conjectura
				for (int j = 0; j < 9; j++)
					if (tab[l][j].getAlgarismo() == LIVRE
							&& tab[l][j].getValidadeAlgarismo(alg))
						conj.addPosicaoValida(l, j);

				// insere conjectura na lista de conjecturas
				insereConjecturaOrdenadamente(conj, conjecturas);
			}

			// identifica conjecturas por coluna
			loopColuna: for (int c = 0; c < 9; c++) {
				// verifica se o algarismo é pendente na coluna
				for (int l = 0; l < 9; l++)
					if (tab[l][c].getAlgarismo() == alg)
						continue loopColuna; // se não é pendente, pula para a
												// próxima coluna

				// cria conjectura para o algarismo na coluna atual
				Conjectura conj = new Conjectura(alg, TipoConjectura.COLUNA);

				// para cada célula livre válida para ser ocupada pelo
				// algarismo, insere esta possibilidade na conjectura
				for (int i = 0; i < 9; i++)
					if (tab[i][c].getAlgarismo() == LIVRE
							&& tab[i][c].getValidadeAlgarismo(alg))
						conj.addPosicaoValida(i, c);

				// insere conjectura na lista de conjecturas
				insereConjecturaOrdenadamente(conj, conjecturas);
			}
		}
	}

	private void insereConjecturaOrdenadamente(Conjectura conj,
			ArrayList<Conjectura> conjecturas) {
		int pos = 0;
		for (Conjectura c : conjecturas) {
			if (c.getAlgarismo() == conj.getAlgarismo()
					&& c.getPosicoesValidas().size() == conj
							.getPosicoesValidas().size()) {
				boolean igual = true;
				for (int i = 0; i < c.getPosicoesValidas().size(); i++)
					if (c.getPosicoesValidas().get(i).intValue() != conj
							.getPosicoesValidas().get(i).intValue()) {
						igual = false;
						break;
					}
				if (igual) {
					pos = -1;
					break;
				}
			}
			if (c.getPosicoesValidas().size() <= conj.getPosicoesValidas()
					.size())
				pos++;
		}
		if (pos >= 0)
			conjecturas.add(pos, conj);
	}

	/**
	 * Este método faz a definição das possibilidades de preenchimento para cada
	 * célula vazia. Para fazer isto, as células vazias são analisadas para
	 * identificar os algarismos que podem ser nelas posicionados, respeitando
	 * as condições de não repetição em bloco, em linha e em coluna. Em seguida,
	 * são feitas limpezas como está descrito a seguir:<br/>
	 * <ol>
	 * <li>Em um bloco, se um algarismo só pode ocorrer em uma linha, este
	 * algarismo não pode ser permitido em outras células livres da mesma linha;
	 * </li>
	 * <li>Em um bloco, se um algarismo só pode ocorrer em uma coluna, este
	 * algarismo não pode ser permitido em outras células livres da mesma
	 * coluna;</li>
	 * <li>Em uma linha, se um algarismo só pode ocorrer em células que
	 * pertencem a um único bloco, as outras células livres do mesmo bloco não
	 * podem permitir este algarismo;</li>
	 * <li>Em uma coluna, se um algarismo só pode ocorrer em células que
	 * pertencem a um único bloco, as outras células livres do mesmo bloco não
	 * podem permitir este algarismo.</li>
	 * </ol>
	 * 
	 * @param tab
	 *            a matriz 9x9 de células que representa o tabuleiro do jogo.
	 */
	private void defAlgarismosValidosCelulasLivres(Celula[][] tab) {
		// faz o preenchimento inicial de algarismos válidos para cada célula
		// livre
		for (int linha = 0; linha < 9; linha++)
			for (int coluna = 0; coluna < 9; coluna++)
				if (tab[linha][coluna].getAlgarismo() == LIVRE) {
					// verifica cada algarismo, se está pendente no bloco, na
					// linha e na coluna
					for (int indAlg = 0; indAlg < 9; indAlg++) {
						char alg = ALGARISMOS[indAlg];
						boolean pendente = true;

						// verifica pendência no bloco
						int linhaBloco = linha / 3, colunaBloco = coluna / 3;
						loopBloco: for (int i = linhaBloco * 3; i < linhaBloco * 3 + 3; i++)
							for (int j = colunaBloco * 3; j < colunaBloco * 3 + 3; j++)
								if (tab[i][j].getAlgarismo() == alg) {
									pendente = false;
									break loopBloco;
								}

						// verifica pendência na linha
						if (pendente) {
							for (int j = 0; j < 9; j++)
								if (tab[linha][j].getAlgarismo() == alg) {
									pendente = false;
									break;
								}
						}

						// verifica pendência na coluna
						if (pendente) {
							for (int i = 0; i < 9; i++)
								if (tab[i][coluna].getAlgarismo() == alg) {
									pendente = false;
									break;
								}
						}

						// marca a validade do algarismo para a célula, de
						// acordo com a pendência
						tab[linha][coluna].setValidadeAlgarismo(alg, pendente);
					}
				}

		// faz a limpezas (1) e (2)
		for (int linhaBloco = 0; linhaBloco < 3; linhaBloco++)
			for (int colunaBloco = 0; colunaBloco < 3; colunaBloco++) {
				for (int indAlg = 0; indAlg < 9; indAlg++) {
					char alg = ALGARISMOS[indAlg];
					int contaLinhas = 0, contaColunas = 0;
					int indLinha = -1, indColuna = -1;
					for (int i = linhaBloco * 3; i < linhaBloco * 3 + 3; i++)
						for (int j = colunaBloco * 3; j < colunaBloco * 3 + 3; j++)
							if (tab[i][j].getAlgarismo() == LIVRE
									&& tab[i][j].getValidadeAlgarismo(alg)) {
								if (i != indLinha) {
									indLinha = i;
									contaLinhas++;
								}
								if (j != indColuna) {
									indColuna = j;
									contaColunas++;
								}
							}
					if (contaLinhas == 1) { // limpeza (1)
						for (int c = 0; c < 9; c++)
							if (c / 3 != colunaBloco
									&& tab[indLinha][c].getAlgarismo() == LIVRE)
								tab[indLinha][c].setValidadeAlgarismo(alg,
										false);
					}
					if (contaColunas == 1) { // limpeza (2)
						for (int l = 0; l < 9; l++)
							if (l / 3 != linhaBloco
									&& tab[l][indColuna].getAlgarismo() == LIVRE)
								tab[l][indColuna].setValidadeAlgarismo(alg,
										false);
					}
				}
			}

		// faz a limpeza (3)
		for (int linha = 0; linha < 9; linha++) {
			for (int indAlg = 0; indAlg < 9; indAlg++) {
				char alg = ALGARISMOS[indAlg];
				int contaBlocos = 0;
				int indColunaBloco = -1;
				for (int coluna = 0; coluna < 9; coluna++)
					if (tab[linha][coluna].getAlgarismo() == LIVRE
							&& tab[linha][coluna].getValidadeAlgarismo(alg)) {
						if (coluna / 3 != indColunaBloco) {
							indColunaBloco = coluna / 3;
							contaBlocos++;
						}
					}
				if (contaBlocos == 1) { // limpeza (3)
					int indLinhaBloco = linha / 3;
					for (int i = indLinhaBloco * 3; i < indLinhaBloco * 3 + 3; i++)
						for (int j = indColunaBloco * 3; j < indColunaBloco * 3 + 3; j++)
							if (i != linha && tab[i][j].getAlgarismo() == LIVRE)
								tab[i][j].setValidadeAlgarismo(alg, false);
				}
			}
		}

		// faz a limpeza (4)
		for (int coluna = 0; coluna < 9; coluna++) {
			for (int indAlg = 0; indAlg < 9; indAlg++) {
				char alg = ALGARISMOS[indAlg];
				int contaBlocos = 0;
				int indLinhaBloco = -1;
				for (int linha = 0; linha < 9; linha++)
					if (tab[linha][coluna].getAlgarismo() == LIVRE
							&& tab[linha][coluna].getValidadeAlgarismo(alg)) {
						if (linha / 3 != indLinhaBloco) {
							indLinhaBloco = linha / 3;
							contaBlocos++;
						}
					}
				if (contaBlocos == 1) { // limpeza (4)
					int indColunaBloco = coluna / 3;
					for (int i = indLinhaBloco * 3; i < indLinhaBloco * 3 + 3; i++)
						for (int j = indColunaBloco * 3; j < indColunaBloco * 3 + 3; j++)
							if (j != coluna
									&& tab[i][j].getAlgarismo() == LIVRE)
								tab[i][j].setValidadeAlgarismo(alg, false);
				}
			}
		}
	}
}
