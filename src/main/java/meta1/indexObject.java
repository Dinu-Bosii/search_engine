package meta1;

public class indexObject {
    String url;
    String titulo;
    String citacao;
    int relevance;

    public indexObject(String url, String titulo, String citacao, int relevance) {
        this.url = url;
        this.titulo = titulo;
        this.citacao = citacao;
        this.relevance = relevance;
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

    public void setrelevance(int relevance) {
        this.relevance = relevance;
    }

    public void addrelevance() {
        this.relevance += 1;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getCitacao() {
        return citacao;
    }

    public int getrelevance() {
        return relevance;
    }
}
