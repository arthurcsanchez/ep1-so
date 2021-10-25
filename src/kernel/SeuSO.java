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

	@Override
	// ATENCÃO: cria o processo mas o mesmo 
	// só estará "pronto" no próximo ciclo
	protected void criaProcesso(Operacao[] codigo) {
		PCB processoAtual = new PCB(contadorProcessos++, contadorCiclos, codigo);
		processos.add(processoAtual);
	}

	@Override
	protected void trocaContexto(PCB pcbAtual, PCB pcbProximo) {
		// ?
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
		Collections.sort(processos);
		contadorCiclos++;
	}

	@Override
	protected boolean temTarefasPendentes() {
		// verificar se há processo em execução ainda
		return false;
	}

	@Override
	protected Integer idProcessoNovo() {
		// ?
		return null;
	}

	@Override
	protected List<Integer> idProcessosProntos() {
		// ?
		return null;
	}

	@Override
	protected Integer idProcessoExecutando() {
		// ?
		return null;
	}

	@Override
	protected List<Integer> idProcessosEsperando() {
		// ?
		return null;
	}

	@Override
	protected List<Integer> idProcessosTerminados() {
		// ?
		return null;
	}

	@Override
	protected int tempoEsperaMedio() {
		// ?
		return 0;
	}

	@Override
	protected int tempoRespostaMedio() {
		// ?
		return 0;
	}

	@Override
	protected int tempoRetornoMedio() {
		// ?
		return 0;
	}

	@Override
	protected int trocasContexto() {
		// ?
		return 0;
	}

	@Override
	public void defineEscalonador(Escalonador e) {
		this.escalonadorAtual = e;
	}
}
