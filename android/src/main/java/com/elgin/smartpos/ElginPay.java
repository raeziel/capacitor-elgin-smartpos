package com.elgin.smartpos;

import com.elgin.smartpos.ElginPayController.ElginPayService;

public class ElginPay {
   static ElginPayService elginPayService = new ElginPayService(SmartPOSPlugin.mContext);

    public static void IniciaVendaDebito(String valor){
        elginPayService.IniciaVendaDebito(valor);
    }

    public static void IniciaVendaCredito(String valor, int tipoFinanciamento, int numeroParcelas){
        elginPayService.IniciaVendaCredito(valor, tipoFinanciamento, numeroParcelas);
    }

    public static void IniciaCancelamentoVenda(String valor, String saleRef, String todayDate){
        elginPayService.IniciaCancelamentoVenda(valor, saleRef, todayDate);
    }

    public static void IniciaOperacaoAdministrativa(){
        elginPayService.IniciaOperacaoAdministrativa();
    }
    public static void  setCustomLayoutOn(){
        elginPayService.setCustomLayoutOn();
    }

    public static void  setCustomLayoutOff(){
        elginPayService.setCustomLayoutOff();
    }
}

