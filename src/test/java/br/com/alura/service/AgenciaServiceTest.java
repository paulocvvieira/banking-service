package br.com.alura.service;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import br.com.alura.domain.Agencia;
import br.com.alura.domain.Endereco;
import br.com.alura.domain.http.AgenciaHttp;
import br.com.alura.domain.http.SituacaoCadastral;
import br.com.alura.exceptions.AgenciaNaoAtivaOuNaoEncontradaException;
import br.com.alura.repository.AgenciaRepository;
import br.com.alura.service.http.SituacaoCadastralHttpService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
public class AgenciaServiceTest {

    @InjectMock
    AgenciaRepository agenciaRepository;

    @InjectMock
    @RestClient
    SituacaoCadastralHttpService situacaoCadastralHttpService;

    @Inject
    AgenciaService agenciaService;


    @Test
    public void naoDeveCadastrarAgenciaSeRespostaClientEhNulo() {
        
        Endereco endereco = new Endereco(1, "Rua A", "Logradouro A", "Complemento A", 123);
        Agencia agencia = new Agencia(1, "Agencia A", "Razao Social A", "123", endereco);

        Mockito.when(situacaoCadastralHttpService.buscarAgenciaPorCnpj("123"))
                .thenReturn(null);

        Assertions.assertThrows(AgenciaNaoAtivaOuNaoEncontradaException.class, () -> agenciaService.cadastrar(agencia));

        Mockito.verify(agenciaRepository, Mockito.never()).persist(agencia);      
    }
    
     @Test
    public void deveCadastrarAgenciaSeRespostaClientEhSituacaoCadastralAtiva() {
        
        Endereco endereco = new Endereco(1, "Rua A", "Logradouro A", "Complemento A", 123);
        Agencia agencia = new Agencia(1, "Agencia A", "Razao Social A", "123", endereco);
        AgenciaHttp agenciaHttp = new AgenciaHttp("Agencia A", "Razao Social A", "123", SituacaoCadastral.ATIVO);
        
        Mockito.when(situacaoCadastralHttpService.buscarAgenciaPorCnpj("123"))
                .thenReturn(agenciaHttp);

        agenciaService.cadastrar(agencia);

        Mockito.verify(agenciaRepository).persist(agencia);      
    }   

}
