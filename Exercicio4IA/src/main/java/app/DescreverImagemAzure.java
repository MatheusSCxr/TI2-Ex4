package app;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class DescreverImagemAzure{
    private static String ENDPOINT = "https://eastus.api.cognitive.microsoft.com/vision/v3.2/describe";
    private static String CHAVE = "";

    public static void main(String[] args) {
        File folder = new File("src/main/resources/imagens");
        File[] images = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpeg")); //fazer um vetor de arquivos (imagens) .jpeg

        if (images != null) {
            for (File image : images) {
                try {
                    String description = describeImage(image); //metodo para fazer a requisição HTTP
                    System.out.println("Descrição da imagem " + image.getName() + ": " + description);
                } catch (IOException e) {
                    System.err.println("Erro em " + image.getName());
                }
            }
        }
    }

    private static String describeImage(File imageFile) throws IOException {
    	//fazer requisição pra API da azure
        HttpURLConnection connection = (HttpURLConnection) new URL(ENDPOINT).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Ocp-Apim-Subscription-Key", CHAVE);  //autenticação
        connection.setRequestProperty("Content-Type", "application/octet-stream"); //especifica o tipo de dados (binários)
        connection.setDoOutput(true);

        //ler os bytes da imagem
        try (OutputStream os = connection.getOutputStream();
             FileInputStream fis = new FileInputStream(imageFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }

        //resposta da API
        
        //erro
        if (connection.getResponseCode() != 200) {
            throw new IOException("ERRO: " + connection.getResponseCode());
        }

        //ler json recebido como resposta
        StringBuilder jsonResponse = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonResponse.append(line);
            }
        }

        connection.disconnect(); //fechar conexão
        
        //retornar string do json
        return jsonResponse.toString().split("\"text\":\"")[1].split("\"")[0];
    }
}
