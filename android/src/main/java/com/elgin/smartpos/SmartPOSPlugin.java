package com.elgin.smartpos;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;

import com.elgin.smartpos.ElginPayController.ElginPayService;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.elgin.smartpos.Scanner.*;

@CapacitorPlugin(name = "SmartPOS")
public class SmartPOSPlugin extends Plugin {

    public static Activity mActivity;
    public static Context mContext;

    //Printer Object
    public static Printer printerInstance;

    //Definindo o Handler que será anexado ao evento do ElginPay para lidar com o retorno do mesmo

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            JSObject ret = new JSObject();
            ret.put("elginpayReturn", msg);

            //chamando o evento que o Ionic se inscreveu pra receber referente ao término da transação
            notifyListeners("elgin_pay_finished", ret);
        }
    };

    @Override
    public void load() {
        super.load();

        //Initilizing all objects

        mActivity = this.getActivity();
        mContext = this.getContext();

        printerInstance = new Printer(mActivity);
        printerInstance.setDefaultImage();

        //Setando o handler criado para a classe que já espera um handler para lidar com o retorno do Elgin Pay
        ElginPayService.handler = this.handler;

    }

    @Override
    protected void handleOnDestroy() {
        super.handleOnDestroy();

        if(printerInstance.statusGaveta() != -4){
            Log.d("DEBUG", "PRINTER INITIALIZED, STOPPING IT...");
            printerInstance.printerStop();
        }
        else{
            Log.d("DEBUG", "PRINTER NOT INITIALIZED, DOING NOTHING ON ONDESTROY()..");
        }
    }


    //Printer --
    @PluginMethod
    public void printImage(PluginCall call){
        JSObject ret = new JSObject();
        Map<String, Object > map = new HashMap<String, Object>();

        map.put("quant", 10);
        int result = this.printerInstance.imprimeImagem();

        ret.put("response", result);

        printerInstance.AvancaLinhas(map);
        if(call.getBoolean("cutPaper")) printerInstance.cutPaper(map);

        call.resolve(ret);
    }

    @PluginMethod
    public void resetDefaultImage(PluginCall call){
        this.printerInstance.setDefaultImage();
        call.resolve();
    }

    @PluginMethod
    public void printerInternalImpStart(PluginCall call) {

        JSObject ret = new JSObject();
        try{
            int result = printerInstance.printerInternalImpStart();
            ret.put("response", result);
            call.resolve(ret);
        }catch (Exception e){
            call.reject("printerInternalImpStart error:" + e.toString());
        }
    }

    @PluginMethod
    public void printerExternalImpStart(PluginCall call){
        JSObject ret = new JSObject();

        Map<String, Object > map = new HashMap<String, Object>();
        try{
            String Ip = call.getString("Ip");


            int dividerIndex = Ip.indexOf(':');
            String ip = Ip.substring(0, dividerIndex);
            String port = Ip.substring(dividerIndex + 1);

            int portAsInt = Integer.parseInt(port);

            map.put("ip", ip);
            map.put("port", portAsInt);

            int result = this.printerInstance.printerExternalImpStart(map);

            ret.put("response", result);
            call.resolve(ret);
        }catch (Exception e){
            call.reject("printerExternalImpStart error:" + e.toString());
        }
    }

    @PluginMethod
    public void printXmlSat(PluginCall call){
        JSObject ret = new JSObject();


        Map<String, Object > map = new HashMap<String, Object>();

        try{
            map.put("xmlSAT",  call.getString( "xmlSAT"));
            Log.d("DEBUG", call.getString("xmlSAT"));
            map.put("param", 0);
            map.put("quant", 10);

            int result = printerInstance.imprimeXMLSAT(map);
            printerInstance.AvancaLinhas(map);

            if(call.getBoolean("cutPaper")) printerInstance.cutPaper(map);


            ret.put("response", result);
            call.resolve(ret);
        }catch (Exception e){
            call.reject("printXmlSat error:"  + e.toString());
        }

    }

    @PluginMethod
    public void printXmlNFCe(PluginCall call){
        JSObject ret = new JSObject();


        Map<String, Object > map = new HashMap<String, Object>();

        try{
            map.put("xmlNFCe",  call.getString( "xmlNFCe"));
            map.put("indexcsc", 1);
            map.put("csc", "CODIGO-CSC-CONTRIBUINTE-36-CARACTERES" );
            map.put("param", 0);
            map.put("quant", 10);

            int result = printerInstance.imprimeXMLNFCe(map);
            printerInstance.AvancaLinhas(map);

            if(call.getBoolean("cutPaper")) printerInstance.cutPaper(map);

            ret.put("response", result);
            call.resolve(ret);
        }catch (Exception e){

            call.reject("printXmlSat error:"  + e.toString());
        }

    }

    @PluginMethod
    public void printText(PluginCall call){
        JSObject ret = new JSObject();

        Map<String, Object > map = new HashMap<String, Object>();

        try{
            map.put("text", call.getString("message"));
            map.put("align", call.getString("alignment"));
            map.put("font", call.getString("font"));
            map.put("fontSize", call.getInt("fontSize"));
            map.put("quant", 10);
            map.put("isBold", call.getBoolean("isBold"));
            map.put("isUnderline", call.getBoolean("isUnderline"));


            int result = printerInstance.imprimeTexto(map);
            printerInstance.AvancaLinhas(map);

            if(call.getBoolean("cutPaper")) printerInstance.cutPaper(map);

            ret.put("response", result);
            call.resolve(ret);
        }catch (Exception e){
            call.reject("printText error:" + e.toString());
        }

    }

    @PluginMethod
    public void printerStatus(PluginCall call){
        JSObject ret = new JSObject();

        int statusSensorPapel = this.printerInstance.statusSensorPapel();


        if(statusSensorPapel == 5) ret.put("response", "Papel está presente e não está próximo do fim!");
        else if (statusSensorPapel == 6) ret.put("reponse" , "Papel próximo do fim!" );
        else if (statusSensorPapel == 7) ret.put("response", "Papel ausente!");
        else ret.put("response", "Status Desconhecido");

        call.resolve(ret);
    }

    @PluginMethod
    public void drawerStatus(PluginCall call){
        JSObject ret = new JSObject();

        int statusGaveta = this.printerInstance.statusGaveta();

        if(statusGaveta == 1) ret.put("response", "Gaveta aberta!");
        else if(statusGaveta == 2) ret.put("response", "Gaveta fechada!");
        else ret.put("response", "Status Desconhecido!");

        call.resolve(ret);
    }

    @PluginMethod
    public void openDrawer(PluginCall call){
        JSObject ret = new JSObject();

        int resultadoAbrirGaveta = this.printerInstance.abrirGaveta();

        ret.put("response", resultadoAbrirGaveta);

        call.resolve(ret);
    }

    @PluginMethod
    public void printBarcode(PluginCall call){
        JSObject ret = new JSObject();

        Map<String, Object > map = new HashMap<String, Object>();

        try{
            if(call.getString("barCodeType").equals("QR CODE")){

                map.put("text", call.getString("code"));
                map.put("qrSize", call.getInt("width"));
                map.put("align", call.getString("alignment"));
                map.put("quant", 10);


                int result = printerInstance.imprimeQR_CODE(map);
                printerInstance.AvancaLinhas(map);

                if(call.getBoolean("cutPaper")) printerInstance.cutPaper(map);

                ret.put("response", result);
            }
            else {

                map.put("barCodeType", call.getString("barCodeType"));
                map.put("text", call.getString("code"));
                map.put("height", call.getInt("height"));
                map.put("width", call.getInt("width"));
                map.put("align", call.getString("alignment"));
                map.put("quant", 10);


                int result = printerInstance.imprimeBarCode(map);
                printerInstance.AvancaLinhas(map);


                if(call.getBoolean("cutPaper")) printerInstance.cutPaper(map);


                ret.put("response", result);
            }
            call.resolve(ret);
        }catch (Exception e){
            call.reject("printBarcode error" + e.toString());
        }
    }

    @PluginMethod
    public void chooseImage(PluginCall call){
        Toast.makeText(mContext, "Selecione uma imagem com no máximo 400 pixels de largura!", Toast.LENGTH_LONG).show();
        Intent intent = new Intent((Intent.ACTION_PICK));
        intent.setType("image/*");
        startActivityForResult(call, intent, "chooseImageResult");
    }

    @ActivityCallback
    private void chooseImageResult(PluginCall call, ActivityResult result) throws IOException {
        if (result.getResultCode() == Activity.RESULT_CANCELED) {
            call.reject("Activity canceled");
        } else {
            Intent intent = result.getData();

            if(intent != null){
                final Uri uri = intent.getData();
                JSObject ret = new JSObject();
                ret.put("imageAsBase64", getBase64FromUri(uri));

                printerInstance.selectedImageBitmap =  MediaStore.Images.Media.getBitmap(this.getContext().getContentResolver(), uri);
                call.resolve(ret);
            }



            call.reject("Houve um erro durante a seleção de imagem!");

        }
    }

    private String getBase64FromUri(Uri uri) throws IOException {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContext().getContentResolver(), uri);

        Bitmap useThis = getResizedBitmap(bitmap,150,150);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        useThis.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();

        return Base64.encodeToString(byteArray, Base64.DEFAULT).replaceAll("\\s","");
    }

    public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    //End of Printer...

    //Elgin Pay --


    @PluginMethod
    public void iniciaVendaDebito(PluginCall pluginCall){
        try{
            String valor = pluginCall.getString("value");

            ElginPay.IniciaVendaDebito(valor);
            pluginCall.resolve();
        }catch (Exception e){
            pluginCall.reject("iniciaVendaDebito error: " + e.toString());
        }
    }

    @PluginMethod
    public void iniciaVendaCredito(PluginCall pluginCall){
        // TYPE OF INSTALLMENTS
        final int FINANCIAMENTO_A_VISTA = 1;
        final int FINANCIAMENTO_PARCELADO_EMISSOR = 2;
        final int FINANCIAMENTO_PARCELADO_ESTABELECIMENTO = 3;


        try{
            String valor = pluginCall.getString("value");
            String stringTipoFinanciamento = pluginCall.getString("typeOfInstallment");
            int numeroParcelas = pluginCall.getInt("numberOfInstallments");

            int tipoFinanciamento = 0;
            Log.d("DEBUG", "value:" + valor + " tipoFinanciamento:" + stringTipoFinanciamento + " numeroParcelas:" + numeroParcelas);

            if(stringTipoFinanciamento.equals("AVista")) tipoFinanciamento = FINANCIAMENTO_A_VISTA;
            else if(stringTipoFinanciamento.equals("Adm")) tipoFinanciamento = FINANCIAMENTO_PARCELADO_EMISSOR;
            else tipoFinanciamento = FINANCIAMENTO_PARCELADO_ESTABELECIMENTO;

            ElginPay.IniciaVendaCredito(valor, tipoFinanciamento, numeroParcelas);
            pluginCall.resolve();

        }catch (Exception e){
            pluginCall.reject("iniciaVendaCredito error: " + e.toString());
        }

    }

    @PluginMethod
    public void iniciaCancelamentoVenda(PluginCall pluginCall){
        try{
            String valor = pluginCall.getString("value");
            String saleRef = pluginCall.getString("saleRef");
            String date = pluginCall.getString("date");

            Log.d("DEBUG", "valor: " + valor + " saleRef: " + saleRef + " date:" + date);

            ElginPay.IniciaCancelamentoVenda(valor, saleRef, date);
            pluginCall.resolve();

        }catch (Exception e){
            pluginCall.reject("iniciaCancelamentoVenda error: " + e.toString());
        }
    }

    @PluginMethod
    public void iniciaOperacaoAdministrativa(PluginCall pluginCall){
        try{
            ElginPay.IniciaOperacaoAdministrativa();
            pluginCall.resolve();
        }catch (Exception e){
            pluginCall.reject("iniciaOperacaoAdministrativa error: " + e.toString());
        }
    }


    @PluginMethod
    public void setCustomLayoutOn(PluginCall pluginCall){
        try{
            ElginPay.setCustomLayoutOn();
            pluginCall.resolve();
        }catch (Exception e){
            pluginCall.reject("setCustomLayoutOn error " + e.toString());
        }
    }

    @PluginMethod
    public void setCustomLayoutOff(PluginCall pluginCall){
        try{
            ElginPay.setCustomLayoutOff();
            pluginCall.resolve();
        }catch (Exception e){
            pluginCall.reject("setCustomLayoutOff error " + e.toString());
        }
    }
    //End of Elgin Pay...



    //Scanner --
    @PluginMethod
    public void initializeScanner(PluginCall pluginCall) {
        Intent intentForScanner = com.elgin.e1.Scanner.Scanner.getScanner(mContext);

        startActivityForResult(pluginCall, intentForScanner, "initializeScannerResult");
    }

    @ActivityCallback
    public void initializeScannerResult(PluginCall pluginCall, ActivityResult result) throws IOException {

        if (result.getResultCode() == Activity.RESULT_CANCELED) {
            Toast.makeText(mActivity, "The scanner activity was canceled!", Toast.LENGTH_SHORT).show();
        } else {
            Intent data = result.getData();
            String[] resultStrings = data.getStringArrayExtra("result");

            Log.d("DEBUG", resultStrings[1] + " " + resultStrings[3]);

            JSObject ret = new JSObject();

            ret.put("code", resultStrings[1]);
            ret.put("codeType", resultStrings[3]);

            pluginCall.resolve(ret);
        }

        pluginCall.reject("Activity canceled!");

    }
    //End of Scanner...


    //Java utilities

    public void alertMessageStatus(String titleAlert, String messageAlert){
        AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
        alertDialog.setTitle(titleAlert);
        alertDialog.setMessage(messageAlert);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    //End of Java utilities

    public static String intentToString(Intent intent) {
        if (intent == null)
            return "";

        StringBuilder stringBuilder = new StringBuilder("action: ")
                .append(intent.getAction())
                .append(" data: ")
                .append(intent.getDataString())
                .append(" extras: ");
        for (String key : intent.getExtras().keySet())
            stringBuilder.append(key).append("=").append(intent.getExtras().get(key)).append(" ");

        return stringBuilder.toString();

    }

}