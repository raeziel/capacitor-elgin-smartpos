 package com.elgin.smartpos.ElginPayController;


import android.content.Context;
import android.content.res.AssetManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.elgin.e1.Pagamento.ElginPay;
import com.elgin.smartpos.SmartPOSPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Random;

import br.com.setis.interfaceautomacao.Confirmacoes;
import br.com.setis.interfaceautomacao.EntradaTransacao;
import br.com.setis.interfaceautomacao.ModalidadesPagamento;
import br.com.setis.interfaceautomacao.Operacoes;
import br.com.setis.interfaceautomacao.SaidaTransacao;
import br.com.setis.interfaceautomacao.Transacoes;
import br.com.setis.interfaceautomacao.Personalizacao;

 public class ElginPayService {


     private Context context;
     ElginPay pagamento = new ElginPay();

     public static Handler handler;

     public ElginPayService(Context c){
         context = c;
     }

     public void IniciaVendaDebito(String valor){
         Toast.makeText(context, "Débito", Toast.LENGTH_LONG).show();

         pagamento.iniciaVendaDebito(valor, context, handler);
     }

     public void IniciaVendaCredito(String valor, int tipoFinanciamento, int numeroParcelas)
     {
         Toast.makeText(context, "Crédito", Toast.LENGTH_LONG).show();

         pagamento.iniciaVendaCredito(valor, tipoFinanciamento, numeroParcelas, context, handler);
     }

     public void IniciaCancelamentoVenda(String valor, String saleRef, String todayDate)
     {
         Toast.makeText(context, "Cancelamento", Toast.LENGTH_LONG).show();

         pagamento.iniciaCancelamentoVenda(valor, saleRef, todayDate, context, handler);
     }

     public void IniciaOperacaoAdministrativa()
     {
         Toast.makeText(context, "Administrativa", Toast.LENGTH_LONG).show();

         pagamento.iniciaOperacaoAdministrativa(context, handler);
     }

     private File createFileFromInputStream(InputStream inputStream) {

        try{
            File f = new File("sdcard/logo2.png");
            OutputStream outputStream = new FileOutputStream(f);
            byte buffer[] = new byte[1024];
            int length = 0;

            while((length=inputStream.read(buffer)) > 0) {
                outputStream.write(buffer,0,length);
            }

            outputStream.close();
            inputStream.close();

            return f;
        }catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

     private Personalizacao obterPersonalizacao(){
        //Processo de personalização do layout
        Personalizacao.Builder pb = new Personalizacao.Builder();
        String corDestaque = "#FED20B"; // AMARELO
        String corPrimaria = "#050609"; // PRETO
        String corSecundaria = "#808080";

        pb.informaCorFonte(corDestaque);
        pb.informaCorFonteTeclado(corPrimaria);
        pb.informaCorFundoToolbar(corDestaque);
        pb.informaCorFundoTela(corPrimaria);
        pb.informaCorTeclaLiberadaTeclado(corDestaque);
        pb.informaCorTeclaPressionadaTeclado(corSecundaria);
        pb.informaCorFundoTeclado(corPrimaria);
        pb.informaCorTextoCaixaEdicao(corDestaque);
        pb.informaCorSeparadorMenu(corDestaque);

        try {
            AssetManager am = context.getAssets();
            InputStream inputStream = am.open("logo_b.png");
            File file = createFileFromInputStream(inputStream);
            pb.informaIconeToolbar(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return pb.build();
    }


     public void setCustomLayoutOn() {
         this.pagamento.setPersonalizacao(obterPersonalizacao());
     }

     public void setCustomLayoutOff() {
         this.pagamento.setPersonalizacao( new Personalizacao.Builder().build());
     }

 }
