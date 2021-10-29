package kernel;
import operacoes.Operacao;
import java.time.LocalTime;
import java.util.Objects;

public class PCB implements Comparable<PCB> {

	public SO.Escalonador escalonadorAtual;
	public enum Estado {NOVO, PRONTO, EXECUTANDO, ESPERANDO, TERMINADO}
	public int idProcesso; // primeiro processo criado deve ter id = 0
	public Estado estado;
	public int[] registradores; // guarda estado dos registradores do processador na troca de contexto
	public int contadorDePrograma;
	public Operacao[] codigo;
	public LocalTime chegadaFilaPronto;

	public int estimativaBurstCPU; // utilizado pelo SJF
	public int contadorBurstCPU; // utilizado para atualizar estimativaBurstCPU e também pelo RR
	public int estimativaTempoRestanteBurstCPU; // utilizado pelo SRTF

	public int estimativaBurstES;
	public int contadorBurstES;
	public int estimativaTempoRestanteBurstES;

	public boolean jaObteveRespostaCPU;

	public int tempoEspera; // incrementar a cada ciclo de pronto
	public int tempoCPU; // incrementar a cada ciclo de executando
	public int tempoES; // incrementar a cada ciclo de esperando
	public int tempoResposta; // tempo até primeiro ciclo de executando

	public PCB(SO.Escalonador escalonadorAtual, int idProcesso, Operacao[] codigo) {
		this.escalonadorAtual = escalonadorAtual;
		this.idProcesso = idProcesso;
		this.estado = Estado.NOVO;
		this.registradores = new int[5];
		this.contadorDePrograma = 0;
		this.codigo = codigo;
		chegadaFilaPronto = LocalTime.now();

		this.estimativaBurstCPU = codigo.length;
		this.estimativaTempoRestanteBurstCPU = codigo.length;
		this.contadorBurstCPU = 0;

		this.estimativaBurstES = codigo.length;
		this.estimativaTempoRestanteBurstES = codigo.length;
		this.contadorBurstES = 0;

		this.jaObteveRespostaCPU = false;

		this.tempoEspera = 0;
		this.tempoCPU = 0;
		this.tempoES = 0;
		this.tempoResposta = 0;
	}

	@Override
	public int compareTo(PCB pcb) {
		switch (escalonadorAtual) {
			case FIRST_COME_FIRST_SERVED:
				if (this.chegadaFilaPronto.isAfter(pcb.chegadaFilaPronto))
					return 1;
				else if (this.chegadaFilaPronto.isBefore(pcb.chegadaFilaPronto))
					return -1;
				else if (this.idProcesso > pcb.idProcesso)
					return 1;
				else if (this.idProcesso < pcb.idProcesso)
					return -1;
				break;
			case SHORTEST_JOB_FIRST:
				if (this.estado == Estado.ESPERANDO) {
					if (this.estimativaBurstES > pcb.estimativaBurstES)
						return 1;
					else if (this.estimativaBurstES < pcb.estimativaBurstES)
						return -1;
					else if (this.idProcesso > pcb.idProcesso)
						return 1;
					else if (this.idProcesso < pcb.idProcesso)
						return -1;
				}
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
				if (this.estado == Estado.ESPERANDO) {
					if (this.estimativaTempoRestanteBurstES > pcb.estimativaTempoRestanteBurstES)
						return 1;
					else if (this.estimativaTempoRestanteBurstES < pcb.estimativaTempoRestanteBurstES)
						return -1;
					else if (this.idProcesso > pcb.idProcesso)
						return 1;
					else if (this.idProcesso < pcb.idProcesso)
						return -1;
				}
				if (this.estimativaTempoRestanteBurstCPU > pcb.estimativaTempoRestanteBurstCPU)
					return 1;
				else if (this.estimativaTempoRestanteBurstCPU < pcb.estimativaTempoRestanteBurstCPU)
					return -1;
				else if (this.idProcesso > pcb.idProcesso)
					return 1;
				else if (this.idProcesso < pcb.idProcesso)
					return -1;
				break;
			// TODO: caso RR
		}
		return 0;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PCB pcb = (PCB) o;
		return idProcesso == pcb.idProcesso && escalonadorAtual == pcb.escalonadorAtual;
	}

	@Override
	public int hashCode() {
		return Objects.hash(escalonadorAtual, idProcesso);
	}

	public void atualizarEstimativaBurstCPU() { // a ser utilizado pelo SJF e SRTF após fim de burst de CPU
		this.estimativaBurstCPU = (this.estimativaBurstCPU + this.contadorBurstCPU) / 2;
		this.contadorBurstCPU = 0;
		this.estimativaTempoRestanteBurstCPU = this.estimativaBurstCPU;
	}

	public void atualizarEstimativaBurstES() { // a ser utilizado pelo SJF e SRTF após fim de burst de CPU
		this.estimativaBurstES = (this.estimativaBurstES + this.contadorBurstES) / 2;
		this.contadorBurstES = 0;
		this.estimativaTempoRestanteBurstES = this.estimativaBurstES;
	}

}
