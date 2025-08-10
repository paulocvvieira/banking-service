package br.com.alura.service;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import br.com.alura.domain.Agencia;
import br.com.alura.domain.http.AgenciaHttp;
import br.com.alura.domain.http.SituacaoCadastral;
import br.com.alura.exceptions.AgenciaNaoAtivaOuNaoEncontradaException;
import br.com.alura.repository.AgenciaRepository;
import br.com.alura.service.http.SituacaoCadastralHttpService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AgenciaService {

    @RestClient
    private SituacaoCadastralHttpService situacaoCadastralHttpService;

    private AgenciaRepository agenciaRepository;

    private final MeterRegistry meterRegistry;

    AgenciaService(AgenciaRepository agenciaRepository, MeterRegistry meterRegistry) {
        this.agenciaRepository = agenciaRepository;
        this.meterRegistry = meterRegistry;
    }

   
    public void cadastrar(Agencia agencia){
        Timer timer = this.meterRegistry.timer("cadastrar_agencia_timer");
        timer.record(() -> {
        AgenciaHttp agenciaHttp = situacaoCadastralHttpService.buscarAgenciaPorCnpj(agencia.getCnpj());
        if(agenciaHttp != null && 
                agenciaHttp.getSituacaoCadastral().equals(SituacaoCadastral.ATIVO)) {
           agenciaRepository.persist(agencia);
           Log.info("Agência com o CNPJ " + agencia.getCnpj() + " cadastrada com sucesso.");
           meterRegistry.counter("agencia_adicionada_counter").increment();
        } else {
            Log.info("Agência com o CNPJ " + agencia.getCnpj() + " não foi cadastrada.");
            meterRegistry.counter("agencia_nao_adicionada_counter").increment();
            throw new AgenciaNaoAtivaOuNaoEncontradaException();
        }
        });

    }

    public Agencia buscarPorId(Long id){
        return agenciaRepository.findById(id);

    }

    public void deletar(Long id){       
        agenciaRepository.deleteById(id);
        Log.info("Agência com o id " + id + " deletada com sucesso.");   
    }

    public void alterar(Agencia agencia) {
        agenciaRepository.update("nome = ?1, razaoSocial = ?2, cnpj = ?3 where id = ?4", agencia.getNome(), agencia.getRazaoSocial(), agencia.getCnpj(), agencia.getId());
        Log.info("Agência com o CNPJ " + agencia.getCnpj() + " alterada com sucesso.");

    }

}
