package meta1;

public class indexObject {
    String url;
    String titulo;
    String citacao;
    int ocorrencias;

    public indexObject(String url, String titulo, String citacao, int ocorrencias) {
        this.url = url;
        this.titulo = titulo;
        this.citacao = citacao;
        this.ocorrencias = ocorrencias;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setCitacao(String citacao) {
        this.citacao = citacao;
    }

    public void setOcorrencias(int ocorrencias) {
        this.ocorrencias = ocorrencias;
    }

    public void addOcorrencias() {
        this.ocorrencias += 1;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getCitacao() {
        return citacao;
    }

    public int getOcorrencias() {
        return ocorrencias;
    }
}
