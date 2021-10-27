package kernel;
import java.util.*;

import operacoes.Carrega;
import operacoes.Operacao;
import operacoes.OperacaoES;
import operacoes.Soma;

public class SeuSO extends SO {

	private int numeroTrocasContexto = 0;
	private int contadorProcessos = 0;
	public Escalonador escalonadorAtual;
	public List<PCB> processos = new LinkedList<>();

	// listas auxiliares SO
	public List<PCB> listaNovos = new LinkedList<>();
	public List<PCB> listaProntos = new LinkedList<>();
	public List<PCB> listaExecutando = new LinkedList<>();
	public List<PCB> listaEsperando = new LinkedList<>();
	public List<PCB> listaTerminados = new LinkedList<>();

	// mapa auxiliar ES
	public Map<Integer, Map<PCB, OperacaoES>> mapaES = new HashMap<>();

	public SeuSO() {
		mapaES.put(0, new LinkedHashMap<>());
		mapaES.put(1, new LinkedHashMap<>());
		mapaES.put(2, new LinkedHashMap<>());
		mapaES.put(3, new LinkedHashMap<>());
		mapaES.put(4, new LinkedHashMap<>());
	}

	@Override
	protected void criaProcesso(Operacao[] codigo) {
		PCB processoAtual = new PCB(escalonadorAtual, contadorProcessos++, codigo);
		processos.add(processoAtual);
	}

	@Override
	protected void trocaContexto(PCB pcbAtual, PCB pcbProximo) {
		int i = 0;
		for (int r : processador.registradores) {
			if (pcbAtual != null)
				pcbAtual.registradores[i] = r;
			if (pcbProximo != null)
				processador.registradores[i] = pcbProximo.registradores[i];
			else
				processador.registradores[i] = 0;
			i++;
		}
		numeroTrocasContexto++;
	}

	@Override
	protected OperacaoES proximaOperacaoES(int idDispositivo) {
		switch (escalonadorAtual) {
			case FIRST_COME_FIRST_SERVED:
				Map<PCB, OperacaoES> dispositivoAtual = mapaES.get(idDispositivo);
				for (Map.Entry<PCB, OperacaoES> e : dispositivoAtual.entrySet()) {
					if (e.getValue().ciclos <= 0) {
						dispositivoAtual.remove(e.getKey(), e.getValue());
						continue;
					}
					return e.getValue();
				}
				break;
			case SHORTEST_JOB_FIRST:
				// TODO: caso SJF
				break;
			case SHORTEST_REMANING_TIME_FIRST:
				// TODO: caso SRTF
				break;
			// TODO: caso RR
		}
		return null;
	}

	@Override
	protected Operacao proximaOperacaoCPU() {

		switch (escalonadorAtual) {
			case FIRST_COME_FIRST_SERVED: // igual ao SJF
				for (PCB p : processos) {
					if (p.estado == PCB.Estado.EXECUTANDO) {
						try {
							p.contadorBurstCPU++;
							return p.codigo[p.contadorDePrograma++];
						} catch (ArrayIndexOutOfBoundsException e) {
							p.contadorBurstCPU--;
							p.estado = PCB.Estado.TERMINADO;
							listaExecutando.remove(p);
							listaTerminados.add(p);
							p.atualizarEstimativaBurstCPU();
							trocaContexto(p, null);
						}
					} else if (p.estado == PCB.Estado.PRONTO) {
						p.estado = PCB.Estado.EXECUTANDO;
						listaProntos.remove(p);
						listaExecutando.add(p);
						trocaContexto(null, p);
						try {
							p.contadorBurstCPU++;
							return p.codigo[p.contadorDePrograma++];
						} catch (ArrayIndexOutOfBoundsException e) {
							p.contadorBurstCPU--;
							p.estado = PCB.Estado.TERMINADO;
							listaExecutando.remove(p);
							listaTerminados.add(p);
							p.atualizarEstimativaBurstCPU();
							trocaContexto(p, null);
						}
					}
				}
				break;
			case SHORTEST_JOB_FIRST: // igual ao FCFS
				for (PCB p : processos) {
					if (p.estado == PCB.Estado.EXECUTANDO) {
						try {
							p.contadorBurstCPU++;
							return p.codigo[p.contadorDePrograma++];
						} catch (ArrayIndexOutOfBoundsException e) {
							p.contadorBurstCPU--;
							p.estado = PCB.Estado.TERMINADO;
							listaExecutando.remove(p);
							listaTerminados.add(p);
							p.atualizarEstimativaBurstCPU();
							trocaContexto(p, null);
						}
					} else if (p.estado == PCB.Estado.PRONTO) {
						p.estado = PCB.Estado.EXECUTANDO;
						listaProntos.remove(p);
						listaExecutando.add(p);
						trocaContexto(null, p);
						try {
							p.contadorBurstCPU++;
							return p.codigo[p.contadorDePrograma++];
						} catch (ArrayIndexOutOfBoundsException e) {
							p.contadorBurstCPU--;
							p.estado = PCB.Estado.TERMINADO;
							listaExecutando.remove(p);
							listaTerminados.add(p);
							p.atualizarEstimativaBurstCPU();
							trocaContexto(p, null);
						}
					}
				}
				break;
			case SHORTEST_REMANING_TIME_FIRST:
				// TODO: refazer
				PCB processoAntigo = null;
				for (PCB p : processos) {
					if (p.estado == PCB.Estado.EXECUTANDO || p.estado == PCB.Estado.PRONTO) {
						if (p.estado == PCB.Estado.PRONTO) {
							for (PCB pAntigo : processos) {
								if (pAntigo.estado == PCB.Estado.EXECUTANDO) {
									processoAntigo = pAntigo;
									break;
								}
							}
							if (processoAntigo != null) {
								processoAntigo.estado = PCB.Estado.PRONTO;
								listaExecutando.remove(processoAntigo);
								listaProntos.add(processoAntigo);
							}
							p.estado = PCB.Estado.EXECUTANDO;
							listaProntos.remove(p);
							listaExecutando.add(p);
							trocaContexto(processoAntigo, p);
						}
						try {
							Operacao prox = p.codigo[p.contadorDePrograma++];
							if (prox instanceof OperacaoES) {
								p.estado = PCB.Estado.ESPERANDO;
								listaExecutando.remove(p);
								listaEsperando.add(p);
								processoAntigo = p;
								p.atualizarEstimativaBurstCPU();
								p.estimativaTempoRestanteBurstCPU = p.estimativaBurstCPU;
								break;
							}
							p.contadorBurstCPU++;
							p.estimativaTempoRestanteBurstCPU--;
							return prox;
						} catch (ArrayIndexOutOfBoundsException e) {
							p.estado = PCB.Estado.TERMINADO;
							listaExecutando.remove(p);
							listaTerminados.add(p);
							processoAntigo = p;
							p.atualizarEstimativaBurstCPU();
							p.estimativaTempoRestanteBurstCPU = p.estimativaBurstCPU;
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
						p.estimativaTempoRestanteBurstCPU--;
						return p.codigo[p.contadorDePrograma++];
					}
				}
				break;
			// TODO: caso RR
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
			}

			int tamBurstES;
			if (p.estado == PCB.Estado.PRONTO) {
				tamBurstES = atualizaMapaES(p);
				if (tamBurstES > 0) { // se há burst de ES onde o contador de programa está posicionado
					p.estado = PCB.Estado.ESPERANDO;
					p.contadorDePrograma += tamBurstES;
					listaProntos.remove(p);
					listaEsperando.add(p);
				}
			} else if (p.estado == PCB.Estado.EXECUTANDO) {
				tamBurstES = atualizaMapaES(p);
				if (tamBurstES > 0) { // se há burst de ES onde o contador de programa está posicionado
					p.estado = PCB.Estado.ESPERANDO;
					p.contadorDePrograma += tamBurstES;
					listaExecutando.remove(p);
					listaEsperando.add(p);
					p.atualizarEstimativaBurstCPU();
					trocaContexto(p, null);
				}
			}

			if (p.estado == PCB.Estado.ESPERANDO) {
				for (Map.Entry<Integer, Map<PCB, OperacaoES>> m : mapaES.entrySet()) {
					if (m.getValue().containsKey(p)) {
						p.espera++;
						return; // ainda há operações no burst de ES inserido no mapa auxiliar
					}
				}
				p.estado = PCB.Estado.PRONTO;
				listaEsperando.remove(p);
				listaProntos.add(p);
			}
		}
	}

	@Override
	protected boolean temTarefasPendentes() {
		return !listaNovos.isEmpty() || !listaProntos.isEmpty() || !listaEsperando.isEmpty() || !listaExecutando.isEmpty();
	}

	@Override
	protected Integer idProcessoNovo() {
		return listaNovos.get(listaNovos.size()-1).idProcesso;
	}

	@Override
	protected List<Integer> idProcessosProntos() {
		List<Integer> resultado = new ArrayList<>();
		for (PCB p : listaProntos) {
			resultado.add(p.idProcesso);
		}
		return resultado;
	}

	@Override
	protected Integer idProcessoExecutando() {
		return listaExecutando.get(listaExecutando.size()-1).idProcesso;
	}

	@Override
	protected List<Integer> idProcessosEsperando() {
		List<Integer> resultado = new ArrayList<>();
		for (PCB p : listaEsperando) {
			resultado.add(p.idProcesso);
		}
		return resultado;
	}

	@Override
	protected List<Integer> idProcessosTerminados() {
		List<Integer> resultado = new ArrayList<>();
		for (PCB p : listaTerminados) {
			resultado.add(p.idProcesso);
		}
		return resultado;
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

	public int atualizaMapaES(PCB p) {
		int i = 0;
		for (Operacao o : p.codigo) {
			if (i >= p.contadorDePrograma) {
				if (o instanceof Soma || o instanceof Carrega)
					break;
				OperacaoES atual = (OperacaoES) o;
				if (!mapaES.get(atual.idDispositivo).containsValue(atual))
					mapaES.get(atual.idDispositivo).put(p, atual);
			}
			i++;
		}
		return (i - p.contadorDePrograma); // tamanho do burst de ES
	}
}
