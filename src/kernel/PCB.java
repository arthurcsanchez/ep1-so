package kernel;
import operacoes.Operacao;

public class PCB {

	public enum Estado {NOVO, PRONTO, EXECUTANDO, ESPERANDO, TERMINADO};
	public int idProcesso; // primeiro processo criado deve ter id = 0
	public Estado estado = Estado.NOVO;
	public int[] registradores = new int[5];
	public int contadorDePrograma;
	public int cicloDeOrigem;
	public Operacao[] codigo;

	public PCB(int idProcesso, Estado estado, int[] registradores, int contadorDePrograma, int cicloDeOrigem, Operacao[] codigo) {
		this.idProcesso = idProcesso;
		this.estado = estado;
		this.registradores = registradores;
		this.contadorDePrograma = contadorDePrograma;
		this.cicloDeOrigem = cicloDeOrigem;
		this.codigo = codigo;
	}

}
