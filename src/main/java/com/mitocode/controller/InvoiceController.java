package com.mitocode.controller;

import com.mitocode.dto.InvoiceDTO;
import com.mitocode.model.Invoice;
import com.mitocode.service.IInvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;

@RestController
@RequestMapping("/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final IInvoiceService service;
    @Qualifier("invoiceMapper")
    private final ModelMapper modelMapper;

    @GetMapping
    public Mono<ResponseEntity<Flux<InvoiceDTO>>> findAll(){
        Flux<InvoiceDTO> fx = service.findAll().map(this::convertToDTO);

        return Mono.just(ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(fx)
        );
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<InvoiceDTO>> findById(@PathVariable String id){
        return service.findById(id)
                .map(this::convertToDTO)
                .map(e -> ResponseEntity
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(e)
                )
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<InvoiceDTO>> save(@Valid @RequestBody InvoiceDTO dto, final ServerHttpRequest req){
        return service.save(converToDocument(dto))
                .map(this::convertToDTO)
                .map(e -> ResponseEntity
                        .created(URI.create(req.getURI() + "/" + e.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(e)
                );
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<InvoiceDTO>> update(@PathVariable String id, @Valid @RequestBody InvoiceDTO dto){
        // invoice.setId(id);
        // return service.update(id, invoice);
        return Mono.just(dto)
                /* .map(e -> {
                    e.setId(id);
                    return e;
                }) */
                .flatMap(e -> {
                    e.setId(id);
                    return service.update(id,converToDocument(dto));
                })
                .map(this::convertToDTO)
                .map(e -> ResponseEntity
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(e)
                )
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Boolean>> delete(@PathVariable String id){
        return service.delete(id)
                .flatMap(result -> {
                    if (result){
                        return Mono.just(ResponseEntity.noContent().build());
                    } else {
                        return Mono.just(ResponseEntity.notFound().build());
                    }
                });
    }

    /* private Invoice invoiceHateoas; */

    @GetMapping("/hateoas/{id}")
    public Mono<EntityModel<Invoice>> getHateoas(@PathVariable String id){
        Mono<Link> monoLink = linkTo(methodOn(InvoiceController.class).findById(id)).withRel("invoice-link").toMono();

        // PRÁCTICA NO RECOMENDADA
        /* return service.findById(id)
                .map(e -> EntityModel.of(e, monoLink.block())); */

        // PRACTICA COMUN, PERO NO IDEAL
        /* return service.findById(id)
                .flatMap(e -> {
                    this.invoiceHateoas = e;
                    return monoLink;
                })
                .map(link -> EntityModel.of(invoiceHateoas, link)); */

        // PRACTICA INTERMEDIA
        /* return service.findById(id)
                .flatMap(e -> monoLink.map(link -> EntityModel.of(e, link))); */

        // PRACTICA IDEAL
        return service.findById(id)
                .zipWith(monoLink, EntityModel::of);
    }

    @GetMapping("/generateReport/{id}")
    public Mono<ResponseEntity<byte[]>> generateReport(@PathVariable String id){
        return service.generateReport(id)
                .map(bytes -> ResponseEntity
                        .ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(bytes)
                )
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    private InvoiceDTO convertToDTO(Invoice invoice){
        return modelMapper.map(invoice, InvoiceDTO.class);
    }

    private Invoice converToDocument(InvoiceDTO invoiceDTO){
        return modelMapper.map(invoiceDTO, Invoice.class);
    }

}
