package kernel;
import operacoes.Operacao;

public class PCB implements Comparable<PCB> {

	public SO.Escalonador escalonadorAtual;
	public enum Estado {NOVO, PRONTO, EXECUTANDO, ESPERANDO, TERMINADO}
	public int idProcesso; // primeiro processo criado deve ter id = 0
	public Estado estado;
	public int[] registradores; // guarda estado dos registradores do processador na troca de contexto
	public int contadorDePrograma;
	public Operacao[] codigo;

	public int estimativaBurstCPU; // utilizado pelo SJF
	public int estimativaTempoRestanteBurstCPU; // utilizado pelo SRTF
	public int contadorBurstCPU; // utilizado para atualizar estimativaBurstCPU e também pelo RR

	public int espera; // incrementar a cada ciclo de espera; utilizado no tempoEsperaMedio()

	public PCB(SO.Escalonador escalonadorAtual, int idProcesso, Operacao[] codigo) {
		this.escalonadorAtual = escalonadorAtual;
		this.idProcesso = idProcesso;
		this.estado = Estado.NOVO;
		this.registradores = new int[5];
		this.contadorDePrograma = 0;
		this.codigo = codigo;
		this.estimativaBurstCPU = codigo.length;
		this.estimativaTempoRestanteBurstCPU = this.estimativaBurstCPU;
		this.contadorBurstCPU = 0;
		this.espera = 0;
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
		}
		return 0;
	}

	public void atualizarEstimativaBurstCPU() { // a ser utilizado pelo SJF após fim de burst de CPU
		this.estimativaBurstCPU = (this.estimativaBurstCPU + this.contadorBurstCPU) / 2;
		this.contadorBurstCPU = 0;
	}

}
