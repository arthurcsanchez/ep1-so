package kernel;
import operacoes.Operacao;

public class PCB implements Comparable<PCB> {

	public SO.Escalonador escalonadorAtual;
	public enum Estado {NOVO, PRONTO, EXECUTANDO, ESPERANDO, TERMINADO}
	public int idProcesso; // primeiro processo criado deve ter id = 0
	public Estado estado;
	public int[] registradores;
	public int contadorDePrograma;
	public int cicloDeOrigem;
	public Operacao[] codigo;

	public int estimativaBurstCPU; // utilizado pelo SJF
	public int estimativaTempoRestanteBurstCPU; // utilizado pelo SRTF
	public int contadorBurstCPU; // utilizado para atualizar estimativaBurstCPU

	public PCB(int idProcesso, int cicloDeOrigem, Operacao[] codigo) {
		this.idProcesso = idProcesso;
		this.estado = Estado.NOVO;
		this.registradores = new int[5];
		this.contadorDePrograma = 0;
		this.cicloDeOrigem = cicloDeOrigem;
		this.codigo = codigo;
		this.estimativaBurstCPU = codigo.length;
		this.estimativaTempoRestanteBurstCPU = this.estimativaBurstCPU;
		this.contadorBurstCPU = 0;
	}

	@Override
	public int compareTo(PCB pcb) {
		switch (escalonadorAtual) {
			case FIRST_COME_FIRST_SERVED:
				if (this.idProcesso > pcb.idProcesso)
					return 1;
				else if (this.idProcesso < pcb.idProcesso)
					return -1;
				break;
			case SHORTEST_JOB_FIRST:
				if (this.estimativaBurstCPU > pcb.estimativaBurstCPU)
					return 1;
				else if (this.estimativaBurstCPU < pcb.estimativaBurstCPU)
					return -1;
				else if (this.idProcesso > pcb.idProcesso)
					return 1;
				else if (this.idProcesso < pcb.idProcesso)
					return -1;
				break;
			case SHORTEST_REMANING_TIME_FIRST:
				if (this.estimativaTempoRestanteBurstCPU > pcb.estimativaTempoRestanteBurstCPU)
					return 1;
				else if (this.estimativaTempoRestanteBurstCPU < pcb.estimativaTempoRestanteBurstCPU)
					return -1;
				else if (this.idProcesso > pcb.idProcesso)
					return 1;
				else if (this.idProcesso < pcb.idProcesso)
					return -1;
				break;
			// falta ROUND_ROBIN_QUANTUM_5 (que provavelmente vai utilizar contadorBurstCPU)
		}
		return 0;
	}

}
