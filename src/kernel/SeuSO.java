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
		PCB processoAtual = new PCB(escalonadorAtual, contadorProcessos++, codigo);
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
		// TODO: buscar próxima operação de ES pelo escalonador definido (a partir de um switch)
		// TODO: após burst de ES, atualizar estado de PCB para PRONTO
		return null;
	}

	@Override
	protected Operacao proximaOperacaoCPU() {
		// TODO: buscar próxima operação de CPU pelo escalonador definido (a partir de um switch)
		// TODO: pegar primeiro pcb da lista de processos com estado pronto/execução (a depender do escalonador)
		// TODO: atualizar contador de programa do pcb

		switch (escalonadorAtual) {
			case FIRST_COME_FIRST_SERVED:
				for (PCB p : processos) {
					if (p.estado == PCB.Estado.EXECUTANDO) {
						try {
							Operacao prox = p.codigo[p.contadorDePrograma++];
							if (prox instanceof OperacaoES) {
								p.estado = PCB.Estado.ESPERANDO;
								listaExecutando.remove(p);
								listaEsperando.add(p);
								return null; // processador entra em espera pelo burst de i/o
							}
							return prox;
						} catch (ArrayIndexOutOfBoundsException e) {
							p.estado = PCB.Estado.TERMINADO;
							listaExecutando.remove(p);
							listaTerminados.add(p);
							break;
						}
					} else if (p.estado == PCB.Estado.ESPERANDO) {
						return null; // processador continua em espera pelo burst de i/o
					}
				}
				processador.registradores = new int[5];
				numeroTrocasContexto++;
				for (PCB p : processos) {
					if (p.estado == PCB.Estado.PRONTO) {
						p.estado = PCB.Estado.EXECUTANDO;
						listaProntos.remove(p);
						listaExecutando.add(p);
						return p.codigo[p.contadorDePrograma++];
					}
				}
				break;
			case SHORTEST_JOB_FIRST:
				PCB processoAntigo = null;
				for (PCB p : processos) {
					if (p.estado == PCB.Estado.EXECUTANDO) {
						try {
							Operacao prox = p.codigo[p.contadorDePrograma++];
							if (prox instanceof OperacaoES) {
								p.estado = PCB.Estado.ESPERANDO;
								listaExecutando.remove(p);
								listaEsperando.add(p);
								processoAntigo = p;
								p.atualizarEstimativaBurstCPU();
								break;
							}
							p.contadorBurstCPU++;
							return prox;
						} catch (ArrayIndexOutOfBoundsException e) {
							p.estado = PCB.Estado.TERMINADO;
							listaExecutando.remove(p);
							listaTerminados.add(p);
							processoAntigo = p;
							p.atualizarEstimativaBurstCPU();
							break;
						}
					}
				}
				for (PCB p : processos) {
					if (p.estado == PCB.Estado.PRONTO) {
						p.estado = PCB.Estado.EXECUTANDO;
						listaProntos.remove(p);
						listaExecutando.add(p);
						trocaContexto(processoAntigo, p);
						return p.codigo[p.contadorDePrograma++];
					}
				}
				break;
			case SHORTEST_REMANING_TIME_FIRST:
				break;
		}

		return null;
	}

	@Override
	protected void executaCicloKernel() {
		Collections.sort(processos);

		for (PCB p : processos) {
			if (p.estado == PCB.Estado.NOVO) {
				if (listaNovos.contains(p)) {
					p.estado = PCB.Estado.PRONTO;
					listaNovos.remove(p);
					listaProntos.add(p);
				} else {
					listaNovos.add(p);
				}
			} else if (p.estado == PCB.Estado.ESPERANDO) {
				p.espera++;
			}
		}
	}

	@Override
	protected boolean temTarefasPendentes() {
		// TODO: verificar se há processo em execução ainda
		return false;
	}

	@Override
	protected Integer idProcessoNovo() {
		// TODO: devolve lista com o id dos PCBs em listaNovos
		return null;
	}

	@Override
	protected List<Integer> idProcessosProntos() {
		// TODO: devolve lista com o id dos PCBs em listaProntos
		return null;
	}

	@Override
	protected Integer idProcessoExecutando() {
		// TODO: devolve lista com o id dos PCBs em listaExecutando
		return null;
	}

	@Override
	protected List<Integer> idProcessosEsperando() {
		// TODO: devolve lista com o id dos PCBs em listaEsperando
		return null;
	}

	@Override
	protected List<Integer> idProcessosTerminados() {
		// TODO: devolve lista com o id dos PCBs em listaTerminados
		return null;
	}

	@Override
	protected int tempoEsperaMedio() {
		// TODO: utilizado nas estatísticas finais
		return 0;
	}

	@Override
	protected int tempoRespostaMedio() {
		// TODO: utilizado nas estatísticas finais
		return 0;
	}

	@Override
	protected int tempoRetornoMedio() {
		// TODO: utilizado nas estatísticas finais
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
