package whitesun.telemetry;

import java.util.ArrayList;
import java.util.List;

public class Protocolo {

    List<Dado> dados = new ArrayList<Dado>();

    public String processaInput(String input) {
        String parteValor = input;
        String parteChecksum = input;
        String processado = input;
        String itemDetectado = input;

        if (input.equals("")) {
            return input;
        }

        int posicaoInicio = input.indexOf("!");

        if (posicaoInicio == -1) {
            return input;
        } else  {
            input = input.substring(posicaoInicio+1);
        }

        int posicaoFim = input.indexOf("@");

        if (posicaoFim == -1) {
            return input;
        } else {
            itemDetectado = input.substring(0, posicaoFim);

            processado = input.substring(posicaoFim+1);
        }

        int posicaoSeparador = itemDetectado.indexOf(';');

        if (posicaoSeparador == -1) {
            return input;
        } else {
            parteValor = itemDetectado.substring(0, posicaoSeparador);
            parteChecksum = itemDetectado.substring(posicaoSeparador+1);
        }

        int posicaoIgualValor = parteValor.indexOf("=");
        int posicaoIgualChecksum = parteChecksum.indexOf("=");

        if (posicaoIgualValor != -1 && posicaoIgualChecksum != -1) {
            String apelido = parteValor.substring(0, posicaoIgualValor);
            boolean apelidoOK = false;
            boolean valorOK = false;
            if (apelido.length() == 3) {
                apelidoOK = true;
            }
            String valor = parteValor.substring(posicaoIgualValor+1);
            String checksum = parteChecksum.substring(posicaoIgualChecksum+1);

            float fValor = 0;
            float fChecksum = 0;
            try {
                fValor = Float.parseFloat(valor);
                fChecksum = Float.parseFloat(checksum);
                valorOK = true;
            } catch(NumberFormatException e) {
                valorOK = false;
            }

            if (apelidoOK && valorOK && fValor==fChecksum) {
                atualizaDados(apelido, fValor);
            }
        }

        if (input.indexOf("!") != -1) {
            return processaInput(processado);
            // TODO: Verificar com o Mariga se isso aqui nao gera um problema de por exemplo nao retornar o primeiro dado processado
        }

        return processado;
        // TODO: Verificar com o Mariga se essa funçao nao ta retornando lixo, isto é, caso a condiçao de dado ideal seja
        // encontrada ("!apelido=valor@") nao resta nada na string "processado".
    }

    public List<Dado> getDados(){

        return dados;
    }

    private void atualizaDados(String apelido, float fvalor) {

        for (int i = 0; i < dados.size(); i++) {
            // Se encontrou um dado com esse nome, adiciona valor
            if(dados.get(i).getApelido().equals(apelido)){
                dados.get(i).addValor(fvalor);
                // Retorna pra não continuar a função
                return;
            }
        }

        // Se não encontrou dado com aquele nome, não retornou e caiu aqui, adiciona novo dado
        dados.add(new Dado(apelido, fvalor));
    }

}
