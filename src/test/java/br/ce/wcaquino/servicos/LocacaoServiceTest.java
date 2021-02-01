package br.ce.wcaquino.servicos;


import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import br.ce.wcaquino.builders.FilmeBuilder;
import br.ce.wcaquino.builders.LocacaoBuilder;
import br.ce.wcaquino.builders.UsuarioBuilder;
import br.ce.wcaquino.dao.LocacaoDao;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import br.ce.wcaquino.matchers.MatchersProprios;
import br.ce.wcaquino.utils.DataUtils;

public class LocacaoServiceTest {

	@InjectMocks
	private LocacaoService service;
	
	@Mock
	private SPCService spcService;
	
	@Mock
	private EmailService emailService;
	
	@Mock
	private LocacaoDao locacaoDao;
	
	@Rule
	public ErrorCollector error = new ErrorCollector();
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		//System.out.println("Before");
	}
	
	/*
	@After
	public void tearDown() {
		System.out.println("After");
	}
	
	@BeforeClass
	public static void setUpClass() {
		System.out.println("BeforeClass");
	}
	
	@AfterClass
	public static void tearDownClass() {
		System.out.println("AfterClass");
	}
	*/
	
	@Test
	public void deveAlugarFilme() throws Exception {
		
		//Assume que esse teste rode em qualquer dia menos no sabado
		Assume.assumeFalse(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));
		
		//cenario
		//Usuario usuario = new Usuario("João");
		Usuario usuario = UsuarioBuilder.umUsuario().agora();
		List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().comValor(5.0).agora());
		
		//acao
		Locacao locacao = service.alugarFilme(usuario, filmes);
		
		//verificacao
		//Assert.assertThat(locacao.getValor(), CoreMatchers.is(5.0));
		//Assert.assertEquals(5.0, locacao.getValor() ,0.01);
		//Assert.assertTrue(DataUtils.isMesmaData(locacao.getDataLocacao(), new Date()));
		//Assert.assertTrue(DataUtils.isMesmaData(locacao.getDataRetorno(), DataUtils.obterDataComDiferencaDias(1)));
		error.checkThat(locacao.getValor(), CoreMatchers.is(5.0));
		//error.checkThat(DataUtils.isMesmaData(locacao.getDataLocacao(), new Date()), is(true));
		//error.checkThat(DataUtils.isMesmaData(locacao.getDataRetorno(), DataUtils.obterDataComDiferencaDias(1)), is(true));
		
		error.checkThat(locacao.getDataRetorno(), MatchersProprios.ehHojeComDiferencaDias(1));
		error.checkThat(locacao.getDataLocacao(), MatchersProprios.ehHoje());
	}
	
	//Necessario garantir que a exceção lançada se deu devido ao erro esperado
	// e não por qlquer outro tipo de erro
	@Test(expected = FilmeSemEstoqueException.class)
	public void deveLancarExcecaoAoAlugarFilmeSemEstoque() throws Exception {
		
		//cenario
		Usuario usuario = new Usuario("João");
		List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().semEstoque().agora());
				
		//acao
		service.alugarFilme(usuario, filmes);
	}
	
	/*
	@Test
	public void testeLocacao_filmeSemEstoque_2() {
		
		//cenario
		Usuario usuario = new Usuario("João");
		List<Filme> filmes = new ArrayList<Filme>();
		
		Filme filme = new Filme("Titanic", 0, 5.0);
		filmes.add(filme);
		
		//acao
		try {
			service.alugarFilme(usuario, filmes);
			// fail inserido pois se o teste for executado em um cenario com estoque
			// o test dará sucesso pois ele executa sem problemas o que não é correto
			Assert.fail("Deveria ter lançado uma exceção");
		} catch (Exception e) {
			assertThat(e.getMessage(), is("Filme sem estoque"));
		}
	}
	*/
	
	/*
	@Test
	public void testeLocacao_filmeSemEstoque_3() throws Exception {
		
		//cenario
		Usuario usuario = new Usuario("João");
		List<Filme> filmes = new ArrayList<Filme>();
		
		Filme filme = new Filme("Titanic", 0, 5.0);
		filmes.add(filme);
		
		exception.expect(Exception.class);
		exception.expectMessage("Filme sem estoque");
		
		//acao
		service.alugarFilme(usuario, filmes);
		
	}
	*/
	
	@Test
	public void naoDeveAlugarFilmeSemUsuario() throws FilmeSemEstoqueException {
		
		//cenario
		List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().agora());
		
		//acao
		try {
			service.alugarFilme(null, filmes);
			Assert.fail();
		}  catch (LocadoraException e) {
			Assert.assertThat(e.getMessage(), CoreMatchers.is("Usuário vazio"));
		}
		
		//System.out.println("Forma Robusta");
	}
	
	@Test
	public void naoDeveAlugarFilmeSemFilme() throws FilmeSemEstoqueException, LocadoraException {
		
		//cenario
		Usuario usuario = UsuarioBuilder.umUsuario().agora();
		
		exception.expect(LocadoraException.class);
		exception.expectMessage("Filme vazio");
		
		//acao
		service.alugarFilme(usuario, null);
		
		//System.out.println("Forma Nova");
	}
	
	/*
	@Test
	public void devePagar75PctNoFilme3() throws FilmeSemEstoqueException, LocadoraException {
		//cenario
		Usuario joao = new Usuario("João");
		List<Filme> filmes = Arrays.asList(new Filme("Rei Leão", 2, 4.0),
				new Filme("Aladin", 2, 4.0),
				new Filme("Toy Story 2", 2, 4.0));
		
		//acao
		Locacao locacao = service.alugarFilme(joao, filmes);
		
		//verificacao
		//4+4+3=11
		Assert.assertThat(locacao.getValor(), CoreMatchers.is(11.0));
	}
	
	@Test
	public void devePagar50PctNoFilme4() throws FilmeSemEstoqueException, LocadoraException {
		//cenario
		Usuario joao = new Usuario("João");
		List<Filme> filmes = Arrays.asList(new Filme("Rei Leão", 2, 4.0),
				new Filme("Aladin", 2, 4.0),
				new Filme("Toy Story 2", 2, 4.0),
				new Filme("Up Altas Aventuras", 2, 4.0));
		
		//acao
		Locacao locacao = service.alugarFilme(joao, filmes);
		
		//verificacao
		//4+4+3+2=13
		Assert.assertThat(locacao.getValor(), CoreMatchers.is(13.0));
	}
	
	@Test
	public void devePagar25PctNoFilme5() throws FilmeSemEstoqueException, LocadoraException {
		//cenario
		Usuario joao = new Usuario("João");
		List<Filme> filmes = Arrays.asList(new Filme("Rei Leão", 2, 4.0),
				new Filme("Aladin", 2, 4.0),
				new Filme("Toy Story 2", 2, 4.0),
				new Filme("Up Altas Aventuras", 2, 4.0),
				new Filme("Divertida Mente", 2, 4.0));
		
		//acao
		Locacao locacao = service.alugarFilme(joao, filmes);
		
		//verificacao
		//4+4+3+2+1=14
		Assert.assertThat(locacao.getValor(), CoreMatchers.is(14.0));
	}
	
	@Test
	public void devePagar0PctNoFilme6() throws FilmeSemEstoqueException, LocadoraException {
		//cenario
		Usuario joao = new Usuario("João");
		List<Filme> filmes = Arrays.asList(new Filme("Rei Leão", 2, 4.0),
				new Filme("Aladin", 2, 4.0),
				new Filme("Toy Story 2", 2, 4.0),
				new Filme("Up Altas Aventuras", 2, 4.0),
				new Filme("Divertida Mente", 2, 4.0),
				new Filme("Branca de Neve", 2, 4.0));
		
		//acao
		Locacao locacao = service.alugarFilme(joao, filmes);
		
		//verificacao
		//4+4+3+2+1+0=14
		Assert.assertThat(locacao.getValor(), CoreMatchers.is(14.0));
	}
	*/
	
	@Test
	//@Ignore
	public void deveDevolverNaSegundaAoAlugarNoSabado() throws FilmeSemEstoqueException, LocadoraException {
		
		//Assume que esse teste rode apenas aos sabados
		Assume.assumeTrue(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));
		
		//cenario
		Usuario usuario = UsuarioBuilder.umUsuario().agora();
		List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().agora());
		
		//acao
		Locacao locacao = service.alugarFilme(usuario, filmes);
		
		//verificacao
		//boolean ehSegunda = DataUtils.verificarDiaSemana(locacao.getDataRetorno(), Calendar.MONDAY);
		//Assert.assertTrue(ehSegunda);
		//Assert.assertThat(locacao.getDataRetorno(), new DiaSemanaMatcher(Calendar.MONDAY));
		//Assert.assertThat(locacao.getDataRetorno(), MatchersProprios.caiEm(Calendar.MONDAY));
		Assert.assertThat(locacao.getDataRetorno(), MatchersProprios.caiNumaSegunda());
		
	}
	
	@Test
	public void naoDeveAlugarFilmeParaNegativadoSPC() throws Exception {
		//cenario
		Usuario usuario = UsuarioBuilder.umUsuario().agora();
		List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().agora());
		
		Mockito.when(spcService.possuiNegativacao(usuario)).thenReturn(true);
		
		exception.expect(LocadoraException.class);
		exception.expectMessage("Usuario Negativado");
		
		//acao
		service.alugarFilme(usuario, filmes);
		
		//verificacao
		Mockito.verify(spcService).possuiNegativacao(usuario);
	}
	
	@Test
	public void deveEnviarEmailParaLocacoesAtrasadas() {
		//cenario
		Usuario usuario = UsuarioBuilder.umUsuario().agora();
		Usuario usuario2 = new Usuario("Usuario 2");
				
		Locacao locacao = new Locacao();
		locacao.setUsuario(usuario);
		locacao.setFilmes(Arrays.asList(FilmeBuilder.umFilme().agora()));
		locacao.setDataRetorno(DataUtils.obterDataComDiferencaDias(-2));
		
		Locacao locacao2 = new Locacao();
		locacao.setUsuario(usuario2);
		locacao.setFilmes(Arrays.asList(FilmeBuilder.umFilme().agora()));
		locacao.setDataRetorno(DataUtils.obterDataComDiferencaDias(1));
		
		List<Locacao> locacoes = Arrays.asList(locacao,locacao2);
		
		Mockito.when(locacaoDao.obterLocacoesPendentes()).thenReturn(locacoes);
		
		//acao
		service.notificarAtrasos();
		
		//verificacao
		Mockito.verify(emailService).notificarAtraso(usuario);
		Mockito.verify(emailService, Mockito.never()).notificarAtraso(usuario2);
	}
	
	@Test
	public void deveTratarErronoSPC() throws Exception {
		//cenario
		Usuario usuario = UsuarioBuilder.umUsuario().agora();
		List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().agora());
		
		Mockito.when(spcService.possuiNegativacao(usuario)).thenThrow(new Exception("Falha"));
		
		//verificacao
		exception.expect(LocadoraException.class);
		exception.expectMessage("Problemas com SPC, tente novamente");
		
		//acao
		service.alugarFilme(usuario, filmes);
		
		
	}
	
	@Test
	public void deveProrrogarUmaLocacao() {
		//cenario
		Locacao locacao = LocacaoBuilder.umLocacao().agora();
		
		//acao
		service.prorrogarLocacao(locacao, 3);
		
		//verificacao
		ArgumentCaptor<Locacao> argCapt = ArgumentCaptor.forClass(Locacao.class);
		Mockito.verify(locacaoDao).salvar(argCapt.capture());
		Locacao locacaoRetornada = argCapt.getValue();
		
		error.checkThat(locacaoRetornada.getValor(), CoreMatchers.is(12.0));
		error.checkThat(locacaoRetornada.getDataLocacao(), MatchersProprios.ehHoje());
		error.checkThat(locacaoRetornada.getDataRetorno(), MatchersProprios.ehHojeComDiferencaDias(3));
		
	}
}
