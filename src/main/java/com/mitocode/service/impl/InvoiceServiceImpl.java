package com.mitocode.service.impl;

import com.mitocode.model.Invoice;
import com.mitocode.repo.IInvoiceRepo;
import com.mitocode.repo.IGenericRepo;
import com.mitocode.service.IInvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl extends CRUDImpl<Invoice, String> implements IInvoiceService {

    private final IInvoiceRepo repo;

    @Override
    protected IGenericRepo<Invoice, String> getRepo() {
        return repo;
    }
}
