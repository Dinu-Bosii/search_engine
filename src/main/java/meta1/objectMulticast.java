package meta1;

import java.io.Serializable;

public class objectMulticast implements Serializable {
    
    public objectMulticast(int id, int word_parts, int url_parts, String type, String[] words, String[] urls,
            String url, String titulo, String citacao) {
        this.id = id;
        this.word_parts = word_parts;
        this.url_parts = url_parts;
        this.type = type;
        this.words = words;
        this.urls = urls;
        this.url = url;
        this.titulo = titulo;
        this.citacao = citacao;
    }
    int id;
    int word_parts;
    int url_parts;
    String type;
    String[] words;
    String[] urls;
    String url;
    String titulo;
    String citacao;
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String[] getWords() {
        return words;
    }
    public void setWords(String[] words) {
        this.words = words;
    }
    public String[] getUrls() {
        return urls;
    }
    public void setUrls(String[] urls) {
        this.urls = urls;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getTitulo() {
        return titulo;
    }
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    public String getCitacao() {
        return citacao;
    }
    public void setCitacao(String citacao) {
        this.citacao = citacao;
    }
    public int getWord_parts() {
        return word_parts;
    }
    public void setWord_parts(int word_parts) {
        this.word_parts = word_parts;
    }
    public int getUrl_parts() {
        return url_parts;
    }
    public void setUrl_parts(int url_parts) {
        this.url_parts = url_parts;
    }

    
}
