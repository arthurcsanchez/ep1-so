package kernel;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import operacoes.Operacao;
import operacoes.OperacaoES;

public class SeuSO extends SO {

	private int contadorProcessos = 0;
	public Escalonador escalonadorAtual;
	public List<PCB> processos = new LinkedList<>();

	// listas auxiliares
	public List<PCB> listaNovos = new LinkedList<>();
	public List<PCB> listaProntos = new LinkedList<>();
	public List<PCB> listaExecutando = new LinkedList<>();
	public List<PCB> listaEsperando = new LinkedList<>();
	public List<PCB> listaTerminados = new LinkedList<>();

	private int numeroTrocasContexto = 0;

	@Override
	protected void criaProcesso(Operacao[] codigo) {
		PCB processoAtual = new PCB(contadorProcessos++, codigo);
		processos.add(processoAtual);
	}

	@Override
	protected void trocaContexto(PCB pcbAtual, PCB pcbProximo) {
		int i = 0;
		for (int r : processador.registradores) {
			pcbAtual.registradores[i] = r;
			processador.registradores[i] = pcbProximo.registradores[i];
			i++;
		}
		numeroTrocasContexto++;
	}

	@Override
	// Assuma que 0 <= idDispositivo <= 4
	protected OperacaoES proximaOperacaoES(int idDispositivo) {
		// buscar próxima operação de ES pelo escalonador definido (a partir de um switch)
		return null;
	}

	@Override
	protected Operacao proximaOperacaoCPU() {
		// buscar próxima operação de CPU pelo escalonador definido (a partir de um switch)
		return null;
	}

	@Override
	protected void executaCicloKernel() {
		// analisa e atualiza estados dos PCBs (ex. coloca NOVO como PRONTO)
		// insere nas listas auxiliares os PCBs (que serão utilizadas nos métodos de id)
		Collections.sort(processos);
	}

	@Override
	protected boolean temTarefasPendentes() {
		// verificar se há processo em execução ainda
		return false;
	}

	@Override
	protected Integer idProcessoNovo() {
		// devolve lista com o id dos PCBs em listaNovos
		return null;
	}

	@Override
	protected List<Integer> idProcessosProntos() {
		// devolve lista com o id dos PCBs em listaProntos
		return null;
	}

	@Override
	protected Integer idProcessoExecutando() {
		// devolve lista com o id dos PCBs em listaExecutando
		return null;
	}

	@Override
	protected List<Integer> idProcessosEsperando() {
		// devolve lista com o id dos PCBs em listaEsperando
		return null;
	}

	@Override
	protected List<Integer> idProcessosTerminados() {
		// devolve lista com o id dos PCBs em listaTerminados
		return null;
	}

	@Override
	protected int tempoEsperaMedio() {
		// utilizado nas estatísticas finais
		return 0;
	}

	@Override
	protected int tempoRespostaMedio() {
		// utilizado nas estatísticas finais
		return 0;
	}

	@Override
	protected int tempoRetornoMedio() {
		// utilizado nas estatísticas finais
		return 0;
	}

	@Override
	protected int trocasContexto() {
		return numeroTrocasContexto;
	}

	@Override
	public void defineEscalonador(Escalonador e) {
		this.escalonadorAtual = e;
	}
}
