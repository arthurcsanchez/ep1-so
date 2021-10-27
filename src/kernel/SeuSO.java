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
		mapaES.put(0, new TreeMap<>());
		mapaES.put(1, new TreeMap<>());
		mapaES.put(2, new TreeMap<>());
		mapaES.put(3, new TreeMap<>());
		mapaES.put(4, new TreeMap<>());
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
			case SHORTEST_JOB_FIRST:
			case SHORTEST_REMANING_TIME_FIRST:
				Map<PCB, OperacaoES> dispositivoAtual = mapaES.get(idDispositivo);
				for (Map.Entry<PCB, OperacaoES> e : dispositivoAtual.entrySet()) {
					if (e.getValue().ciclos <= 0) {
						dispositivoAtual.remove(e.getKey(), e.getValue());
						continue;
					}
					e.getKey().contadorBurstES++;
					e.getKey().estimativaTempoRestanteBurstES--;
					return e.getValue();
				}
				break;
			// TODO: caso RR
		}
		return null;
	}

	@Override
	protected Operacao proximaOperacaoCPU() {

		switch (escalonadorAtual) {
			case FIRST_COME_FIRST_SERVED: // FCFS e SJF tratados igualmente
			case SHORTEST_JOB_FIRST:
				for (PCB p : processos) {
					if (p.estado == PCB.Estado.EXECUTANDO) {
						try {
							p.contadorBurstCPU++;
							p.estimativaTempoRestanteBurstCPU--;
							return p.codigo[p.contadorDePrograma++];
						} catch (ArrayIndexOutOfBoundsException ignored) {}
					} else if (p.estado == PCB.Estado.PRONTO) {
						p.estado = PCB.Estado.EXECUTANDO;
						listaProntos.remove(p);
						listaExecutando.add(p);
						trocaContexto(null, p);
						try {
							p.contadorBurstCPU++;
							p.estimativaTempoRestanteBurstCPU--;
							return p.codigo[p.contadorDePrograma++];
						} catch (ArrayIndexOutOfBoundsException ignored) {}
					}
				}
				break;
			case SHORTEST_REMANING_TIME_FIRST: // um pouco diferente do FCFS e do SJF
				for (PCB p : processos) {
					if (p.estado == PCB.Estado.EXECUTANDO || p.estado == PCB.Estado.PRONTO) {
						if (p.estado == PCB.Estado.PRONTO) {
							for (PCB pAntigo : processos) {
								if (pAntigo.estado == PCB.Estado.EXECUTANDO) {
									pAntigo.estado = PCB.Estado.PRONTO;
									listaExecutando.remove(pAntigo);
									listaProntos.add(pAntigo);
									pAntigo.atualizarEstimativaBurstCPU();
									trocaContexto(pAntigo, null);
									break;
								}
							}
							p.estado = PCB.Estado.EXECUTANDO;
							listaProntos.remove(p);
							listaExecutando.add(p);
							trocaContexto(null, p);
						}
						try {
							p.contadorBurstCPU++;
							p.estimativaTempoRestanteBurstCPU--;
							return p.codigo[p.contadorDePrograma++];
						} catch (ArrayIndexOutOfBoundsException ignored) {}
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
			if (p.estado == PCB.Estado.NOVO) { // verifica a lista em que um processo novo deve ser colocado
				// TODO problema: quando executa no primeiro ciclo a primeira operação, ela é NOVO, ou seja, proximaOperacaoCPU() devolve null
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
				p.atualizarEstimativaBurstES();
			}

			if (p.estado == PCB.Estado.PRONTO || p.estado == PCB.Estado.EXECUTANDO) { // verifica se processo já acabou
				try {
					Operacao teste = p.codigo[p.contadorDePrograma];
				} catch (ArrayIndexOutOfBoundsException e) {
					p.estado = PCB.Estado.TERMINADO;
					switch (p.estado) {
						case PRONTO: listaProntos.remove(p); break;
						case EXECUTANDO: listaExecutando.remove(p); break;
					}
					listaTerminados.add(p);
					p.atualizarEstimativaBurstCPU();
					trocaContexto(p, null);
				}
			}
		}

		Collections.sort(processos);
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
