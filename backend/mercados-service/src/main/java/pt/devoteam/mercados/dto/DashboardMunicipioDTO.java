package pt.devoteam.mercados.dto;

public class DashboardMunicipioDTO {
    private double totalFaturadoProximaFeira;
    private long feirantesAguardandoAprovacao;
    private long totalVagasDisponiveis;

    public DashboardMunicipioDTO(double totalFaturadoProximaFeira, long feirantesAguardandoAprovacao, long totalVagasDisponiveis) {
        this.totalFaturadoProximaFeira = totalFaturadoProximaFeira;
        this.feirantesAguardandoAprovacao = feirantesAguardandoAprovacao;
        this.totalVagasDisponiveis = totalVagasDisponiveis;
    }

    // Getters e Setters
    public double getTotalFaturadoProximaFeira() { return totalFaturadoProximaFeira; }
    public void setTotalFaturadoProximaFeira(double totalFaturadoProximaFeira) { this.totalFaturadoProximaFeira = totalFaturadoProximaFeira; }
    public long getFeirantesAguardandoAprovacao() { return feirantesAguardandoAprovacao; }
    public void setFeirantesAguardandoAprovacao(long feirantesAguardandoAprovacao) { this.feirantesAguardandoAprovacao = feirantesAguardandoAprovacao; }
    public long getTotalVagasDisponiveis() { return totalVagasDisponiveis; }
    public void setTotalVagasDisponiveis(long totalVagasDisponiveis) { this.totalVagasDisponiveis = totalVagasDisponiveis; }
}