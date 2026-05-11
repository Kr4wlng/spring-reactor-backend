package com.mitocode.service.impl;

import com.mitocode.model.Invoice;
import com.mitocode.model.InvoiceDetail;
import com.mitocode.repo.IClientRepo;
import com.mitocode.repo.IDishRepo;
import com.mitocode.repo.IInvoiceRepo;
import com.mitocode.repo.IGenericRepo;
import com.mitocode.service.IInvoiceService;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl extends CRUDImpl<Invoice, String> implements IInvoiceService {

    private final IInvoiceRepo invoiceRepo;
    private final IClientRepo clientRepo;
    private final IDishRepo dishRepo;

    @Override
    protected IGenericRepo<Invoice, String> getRepo() {
        return invoiceRepo;
    }

    @Override
    public Mono<byte[]> generateReport(String idInvoice) {
        return invoiceRepo.findById(idInvoice)
                .flatMap(this::populateClient)
                .flatMap(this::populateDishes)
                .map(this::generatePDF);
    }

    private Mono<Invoice> populateClient(Invoice invoice){
        return clientRepo.findById(invoice.getClient().getId())
                .map(client -> {
                    invoice.setClient(client);
                    return invoice;
                });
    }

    private Mono<Invoice> populateDishes(Invoice invoice){
        List<Mono<InvoiceDetail>> list = invoice.getItems().stream()
                .map(item ->
                        dishRepo.findById(item.getDish().getId())
                                .map(dish -> {
                                    item.setDish(dish);
                                    return item;
                                })
                ).toList();
        return Mono.when(list).thenReturn(invoice);
    }

    private byte[] generatePDF(Invoice invoice){
        try(InputStream stream = getClass().getResourceAsStream("/facturas.jrxml")){
            Map<String, Object> params = new HashMap<>();
            params.put("txt_client", invoice.getClient().getFirstName() + " " + invoice.getClient().getLastName());

            JasperReport report = JasperCompileManager.compileReport(stream);
            JasperPrint print = JasperFillManager.fillReport(report, params, new JRBeanCollectionDataSource(invoice.getItems()));
            return JasperExportManager.exportReportToPdf(print);
        }catch (Exception e){
            return new byte[0];
        }
    }

    /* @Override
    public Mono<byte[]> generateReport(String idInvoice){
        return invoiceRepo.findById(idInvoice)
                // Obteniendo Client
                .flatMap(inv -> Mono.just(inv)
                        .zipWith(clientRepo.findById(inv.getClient().getId()), (in, cl) -> {
                            in.setClient(cl);
                            return in;
                        })
                )
                // Obteniendo Dishes
                .flatMap( inv ->
                        Flux.fromIterable(inv.getItems())
                                .flatMap(item ->
                                        dishRepo.findById(item.getDish().getId())
                                                .map(d -> {
                                                    item.setDish(d);
                                                    return item;
                                                })
                                ).collectList()
                                .flatMap(list -> {
                                    inv.setItems(list);
                                    return Mono.just(inv);
                                })
                )
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
    } */
}
