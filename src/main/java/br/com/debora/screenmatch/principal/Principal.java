package br.com.debora.screenmatch.principal;

import br.com.debora.screenmatch.model.DadosEpisodios;
import br.com.debora.screenmatch.model.DadosSerie;
import br.com.debora.screenmatch.model.DadosTemporada;
import br.com.debora.screenmatch.model.Episodio;
import br.com.debora.screenmatch.service.ConsumoAPI;
import br.com.debora.screenmatch.service.ConverteDados;

import javax.swing.event.ListDataEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner scanner = new Scanner(System.in);

    private  ConsumoAPI consumo = new ConsumoAPI();
    private ConverteDados conversor = new ConverteDados();

    private final String ENDERECO ="https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6c3e49be";

    public  void exibeMenu() {
        System.out.println("Digite o nome da série para busca: ");
        var nomeSerie = scanner.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") +API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        System.out.println(dados);

        List<DadosTemporada> temporadas = new ArrayList<>();

		for (int i = 1; i <= dados.totalTemporadas(); i++) {
			json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") +"&season=" + i + API_KEY);
			DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
			temporadas.add(dadosTemporada);
		}
		temporadas.forEach(System.out::println);

//        for (int i = 0; i < dados.totalTemporadas(); i++) {
//            List<DadosEpisodios> episodiosTemporada = temporadas.get(i).episodios();
//
//            for (int j = 0; j < episodiosTemporada.size(); j++) {
//                System.out.println(episodiosTemporada.get(j).titulo());
//            }
//        }

        temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));

        List<DadosEpisodios> dadosEpisodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream())
                .collect(Collectors.toList());

//        System.out.println("\nTop 10 episódios");
//        dadosEpisodios.stream()
//                .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
//                .peek(e-> System.out.println("Primeiro filtro(N/A) " + e))
//                .sorted(Comparator.comparing(DadosEpisodios::avaliacao).reversed())
//                .peek(e-> System.out.println("Ordenação " + e))
//                .limit(10)
//                .peek(e-> System.out.println("Limite " + e))
//                .map(e -> e.titulo().toUpperCase())
//                .peek(e-> System.out.println("Mapeamento " + e))
//                .forEach(System.out::println);

        List<Episodio> episodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream()
                        .map(d -> new Episodio(t.numero(), d))
                ).collect(Collectors.toList());

        episodios.forEach(System.out::println);

//        System.out.println("Digite um episodio para busca: ");
//        var trechoTitulo = scanner.nextLine();
//        Optional<Episodio> episodioBuscado = episodios.stream()
//                .filter(e -> e.getTitulo().toUpperCase().contains(trechoTitulo.toUpperCase()))
//                .findFirst();
//
//        if (episodioBuscado.isPresent()){
//            System.out.println("Episodio Encontrado ");
//            System.out.println("Temporada" +episodioBuscado.get().getTemporada());
//        } else {
//            System.out.println("Episodio não encontrado");
//        }
//
//        System.out.println("Deseja ver os episodios a partir de que ano?");
//        var ano = scanner.nextInt();
//        scanner.nextLine();
//
//        LocalDate dataBusca = LocalDate.of(ano, 1, 1);
//
//        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//
//        episodios.stream()
//                .filter(e -> e.getDataLancamento() !=null && e.getDataLancamento().isAfter(dataBusca))
//                .forEach(e -> System.out.println(
//                        "Temporada: " +e.getTemporada() +
//                                " Episodio: " + e.getTitulo() +
//                                " Data lanaçamento: " +e.getDataLancamento().format(formatador)
//                ));
//

        Map<Integer, Double> avaliacoesTemporada = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors.groupingBy(Episodio::getTemporada,
                        Collectors.averagingDouble(Episodio::getAvaliacao)));

        System.out.println(avaliacoesTemporada);

        DoubleSummaryStatistics est = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .peek(e -> System.out.println("Filtro" +e))
                .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));

        System.out.println("Media: " +est.getAverage());
        System.out.println("Melhor episódio: " +est.getMax());
        System.out.println("Pior episódio: " +est.getMin());
        System.out.println("Episodios avaliados: " +est.getCount());


    }
}
