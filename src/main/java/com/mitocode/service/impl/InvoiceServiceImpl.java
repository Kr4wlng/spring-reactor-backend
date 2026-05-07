package com.mitocode.service.impl;

import com.mitocode.model.Invoice;
import com.mitocode.repo.IInvoiceRepo;
import com.mitocode.repo.IGenericRepo;
import com.mitocode.service.IInvoiceService;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl extends CRUDImpl<Invoice, String> implements IInvoiceService {

    private final IInvoiceRepo invoiceRepo;

    @Override
    protected IGenericRepo<Invoice, String> getRepo() {
        return invoiceRepo;
    }

    @Override
    public Mono<byte[]> generateReport(String idInvoice){
        return invoiceRepo.findById(idInvoice)
                .map(inv -> {

                    try {
                        Map<String, Object> params = new HashMap<>();
                        params.put("txt_client", inv.getClient().getFirstName() + " " + inv.getClient().getLastName());

                        InputStream jxrml = getClass().getResourceAsStream("/factura.jxrml");
                        JasperReport jasper = JasperCompileManager.compileReport(jxrml);
                        JasperPrint print = JasperFillManager.fillReport(jasper, params, new JRBeanCollectionDataSource(inv.getItems()));
                        return JasperExportManager.exportReportToPdf(print);
                    } catch (Exception e){
                        return new byte[0];
                    }

                });
    }
}
