package br.unicamp.ic.mc322.persistence;

import br.unicamp.ic.mc322.exception.PersistenciaException;
import br.unicamp.ic.mc322.model.Carroceria;
import br.unicamp.ic.mc322.model.Componente;
import br.unicamp.ic.mc322.model.Configuracao;
import br.unicamp.ic.mc322.model.Jogador;
import br.unicamp.ic.mc322.model.Motor;
import br.unicamp.ic.mc322.model.Roda;
import br.unicamp.ic.mc322.model.Suspensao;
import br.unicamp.ic.mc322.model.Veiculo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Gerencia a persistência de dados do jogo em dois formatos distintos:
 * arquivos de texto estruturado (XML) para múltiplos perfis de {@link Jogador}
 * e arquivo binário serializado para as preferências globais de {@link Configuracao}.
 *
 * @author Luis Gustavo
 */
public class GerenciadorSave {

    private static final String EXTENSAO_PERFIL = ".xml";

    private String diretorioSaves;
    private String caminhoConfigBinario;

    /**
     * Inicializa o gerenciador definindo o diretório onde os perfis XML serão salvos
     * e o caminho do arquivo binário de configurações.
     *
     * @param diretorioSaves       Caminho da pasta destinada aos arquivos de perfil dos jogadores.
     * @param caminhoConfigBinario Caminho completo do arquivo binário de configurações.
     * @throws IllegalArgumentException Se algum dos caminhos informados for nulo ou vazio.
     */
    public GerenciadorSave(String diretorioSaves, String caminhoConfigBinario) {
        if (diretorioSaves == null || diretorioSaves.trim().isEmpty()) {
            throw new IllegalArgumentException("O diretório de saves não pode ser nulo ou vazio.");
        }
        if (caminhoConfigBinario == null || caminhoConfigBinario.trim().isEmpty()) {
            throw new IllegalArgumentException("O caminho do arquivo binário de configuração não pode ser nulo ou vazio.");
        }
        this.diretorioSaves = diretorioSaves.trim();
        this.caminhoConfigBinario = caminhoConfigBinario.trim();
    }

    /**
     * Grava o estado completo de um perfil de jogador (progresso, veículo ativo,
     * inventário polimórfico e recordes de pista) em um arquivo XML no diretório de saves.
     *
     * @param jogador Instância do {@link Jogador} a ser persistida.
     * @throws PersistenciaException Se o jogador for nulo ou ocorrer erro na escrita do XML.
     */
    public void salvarJogadorTexto(Jogador jogador) {
        if (jogador == null) {
            throw new PersistenciaException("Erro ao salvar perfil: o jogador não pode ser nulo.");
        }

        File pasta = new File(this.diretorioSaves);
        if (!pasta.exists() && !pasta.mkdirs()) {
            throw new PersistenciaException("Não foi possível criar o diretório de saves: " + this.diretorioSaves);
        }

        File arquivoDestino = new File(pasta, sanitizarNomeArquivo(jogador.getNome()) + EXTENSAO_PERFIL);

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element raiz = doc.createElement("jogador");
            raiz.setAttribute("nome", jogador.getNome());
            raiz.setAttribute("moedas", String.valueOf(jogador.getMoedas()));
            raiz.setAttribute("xp", String.valueOf(jogador.getXp()));
            raiz.setAttribute("nivel", String.valueOf(jogador.getNivel()));
            doc.appendChild(raiz);

            // 1. Veículo ativo e seus 4 componentes (Composição)
            Veiculo veiculo = jogador.getVeiculoAtivo();
            List<Componente> inventario = jogador.getInventarioComponentes();

            Carroceria carroceriaAtiva = veiculo.getCarroceria();
            Motor motorAtivo = veiculo.getMotor();
            Roda rodaAtiva = veiculo.getRoda();
            Suspensao suspensaoAtiva = veiculo.getSuspensao();

            Element veiculoElem = doc.createElement("veiculoAtivo");
            veiculoElem.appendChild(criarElementoComponente(doc, "carroceria", carroceriaAtiva,
                    inventario.contains(carroceriaAtiva)));
            veiculoElem.appendChild(criarElementoComponente(doc, "motor", motorAtivo,
                    inventario.contains(motorAtivo)));
            veiculoElem.appendChild(criarElementoComponente(doc, "roda", rodaAtiva,
                    inventario.contains(rodaAtiva)));
            veiculoElem.appendChild(criarElementoComponente(doc, "suspensao", suspensaoAtiva,
                    inventario.contains(suspensaoAtiva)));
            raiz.appendChild(veiculoElem);

            // 2. Demais componentes armazenados no inventário da garagem (Agregação)
            Element inventarioElem = doc.createElement("inventario");
            for (Componente comp : inventario) {
                boolean jaSalvoNoVeiculo = (comp == carroceriaAtiva)
                        || (comp == motorAtivo)
                        || (comp == rodaAtiva)
                        || (comp == suspensaoAtiva);
                if (!jaSalvoNoVeiculo) {
                    inventarioElem.appendChild(criarElementoComponente(doc, "componente", comp, true));
                }
            }
            raiz.appendChild(inventarioElem);

            // 3. Recordes de melhor tempo por pista
            Element recordesElem = doc.createElement("recordes");
            for (Map.Entry<String, Double> entrada : jogador.getRecordesPistas().entrySet()) {
                Element recElem = doc.createElement("recorde");
                recElem.setAttribute("pista", entrada.getKey());
                recElem.setAttribute("tempo", String.valueOf(entrada.getValue()));
                recordesElem.appendChild(recElem);
            }
            raiz.appendChild(recordesElem);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            transformer.transform(new DOMSource(doc), new StreamResult(arquivoDestino));

        } catch (ParserConfigurationException | TransformerException | RuntimeException erroEscrita) {
            throw new PersistenciaException(
                    "Falha ao gravar o arquivo XML do jogador '" + jogador.getNome() + "'.", erroEscrita);
        }
    }

    /**
     * Carrega, valida e reconstrói um perfil de {@link Jogador} a partir do arquivo XML correspondente.
     *
     * @param nomePerfil Nome do perfil salvo (sem a extensão {@code .xml}).
     * @return Instância reconstruída do {@link Jogador}.
     * @throws PersistenciaException Se o nome for inválido ou o arquivo estiver ausente, vazio ou corrompido.
     */
    public Jogador carregarJogadorTexto(String nomePerfil) {
        if (nomePerfil == null || nomePerfil.trim().isEmpty()) {
            throw new PersistenciaException("O nome do perfil a carregar não pode ser nulo ou vazio.");
        }

        File arquivoOrigem = new File(this.diretorioSaves, sanitizarNomeArquivo(nomePerfil) + EXTENSAO_PERFIL);
        if (!arquivoOrigem.exists() || !arquivoOrigem.isFile()) {
            throw new PersistenciaException("Perfil não encontrado: " + arquivoOrigem.getPath());
        }
        if (arquivoOrigem.length() == 0) {
            throw new PersistenciaException("O arquivo de save do perfil está vazio: " + arquivoOrigem.getPath());
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(arquivoOrigem);
            doc.getDocumentElement().normalize();

            Element raiz = doc.getDocumentElement();
            if (!"jogador".equals(raiz.getNodeName())) {
                throw new PersistenciaException("Estrutura XML inválida: o elemento raiz deve ser <jogador>.");
            }

            String nome = obterAtributoObrigatorio(raiz, "nome");
            int moedas = Integer.parseInt(obterAtributoObrigatorio(raiz, "moedas"));
            int xp = Integer.parseInt(obterAtributoObrigatorio(raiz, "xp"));
            int nivel = Integer.parseInt(obterAtributoObrigatorio(raiz, "nivel"));

            // 1. Reconstrói o Veículo ativo
            Element veiculoElem = obterElementoFilhoObrigatorio(raiz, "veiculoAtivo");
            Element carroceriaElem = obterElementoFilhoObrigatorio(veiculoElem, "carroceria");
            Element motorElem = obterElementoFilhoObrigatorio(veiculoElem, "motor");
            Element rodaElem = obterElementoFilhoObrigatorio(veiculoElem, "roda");
            Element suspensaoElem = obterElementoFilhoObrigatorio(veiculoElem, "suspensao");

            Carroceria carroceria = (Carroceria) reconstruirComponente(carroceriaElem);
            Motor motor = (Motor) reconstruirComponente(motorElem);
            Roda roda = (Roda) reconstruirComponente(rodaElem);
            Suspensao suspensao = (Suspensao) reconstruirComponente(suspensaoElem);

            Veiculo veiculo = new Veiculo(carroceria, motor, roda, suspensao);
            Jogador jogador = new Jogador(nome, veiculo);
            jogador.restaurarProgresso(moedas, xp, nivel);

            // Reassocia as peças equipadas ao inventário caso estivessem nele ao salvar
            if (Boolean.parseBoolean(carroceriaElem.getAttribute("noInventario"))) {
                jogador.adicionarComponente(carroceria);
            }
            if (Boolean.parseBoolean(motorElem.getAttribute("noInventario"))) {
                jogador.adicionarComponente(motor);
            }
            if (Boolean.parseBoolean(rodaElem.getAttribute("noInventario"))) {
                jogador.adicionarComponente(roda);
            }
            if (Boolean.parseBoolean(suspensaoElem.getAttribute("noInventario"))) {
                jogador.adicionarComponente(suspensao);
            }

            // 2. Reconstrói os demais componentes do inventário
            NodeList invList = raiz.getElementsByTagName("inventario");
            if (invList.getLength() > 0) {
                Element invElem = (Element) invList.item(0);
                NodeList compNodes = invElem.getElementsByTagName("componente");
                for (int i = 0; i < compNodes.getLength(); i++) {
                    Element compElem = (Element) compNodes.item(i);
                    jogador.adicionarComponente(reconstruirComponente(compElem));
                }
            }

            // 3. Reconstrói os recordes de pista
            NodeList recNodes = raiz.getElementsByTagName("recorde");
            for (int i = 0; i < recNodes.getLength(); i++) {
                Element recElem = (Element) recNodes.item(i);
                String nomePista = obterAtributoObrigatorio(recElem, "pista");
                double tempo = Double.parseDouble(obterAtributoObrigatorio(recElem, "tempo"));
                jogador.registrarRecorde(nomePista, tempo);
            }

            return jogador;

        } catch (PersistenciaException erroPersistencia) {
            throw erroPersistencia;
        } catch (IllegalArgumentException | ClassCastException erroDominio) {
            throw new PersistenciaException(
                    "Os dados do XML violam regras de domínio do jogo: " + erroDominio.getMessage(), erroDominio);
        } catch (SAXException | IOException | ParserConfigurationException erroLeitura) {
            throw new PersistenciaException(
                    "Arquivo XML de perfil malformado, corrompido ou ilegível: " + erroLeitura.getMessage(), erroLeitura);
        }
    }

    /**
     * Lista os nomes de todos os perfis de jogadores salvos em XML no diretório de saves.
     *
     * @return Lista ordenada de nomes de perfis disponíveis (vazia se ainda não houver saves).
     */
    public List<String> listarPerfisSalvos() {
        File pasta = new File(this.diretorioSaves);
        if (!pasta.exists() || !pasta.isDirectory()) {
            return Collections.emptyList();
        }

        File[] arquivos = pasta.listFiles((dir, nomeArquivo) -> nomeArquivo.toLowerCase().endsWith(EXTENSAO_PERFIL));
        if (arquivos == null || arquivos.length == 0) {
            return Collections.emptyList();
        }

        List<String> perfis = new ArrayList<>();
        for (File arquivo : arquivos) {
            if (arquivo.isFile()) {
                String nome = arquivo.getName();
                perfis.add(nome.substring(0, nome.length() - EXTENSAO_PERFIL.length()));
            }
        }
        Collections.sort(perfis);
        return perfis;
    }

    /**
     * Persiste o objeto de {@link Configuracao} global do jogo em formato binário.
     *
     * @param config Instância de configuração a ser serializada em disco.
     * @throws PersistenciaException Se a configuração for nula ou ocorrer falha de escrita.
     */
    public void salvarConfigBinario(Configuracao config) {
        if (config == null) {
            throw new PersistenciaException("Erro ao salvar configurações: o objeto Configuracao não pode ser nulo.");
        }

        File arquivo = new File(this.caminhoConfigBinario);
        File pastaPai = arquivo.getParentFile();
        if (pastaPai != null && !pastaPai.exists() && !pastaPai.mkdirs()) {
            throw new PersistenciaException("Não foi possível criar o diretório de configuração: " + pastaPai.getPath());
        }

        try (ObjectOutputStream escritor = new ObjectOutputStream(new FileOutputStream(arquivo))) {
            escritor.writeObject(config);
        } catch (IOException erroEscrita) {
            throw new PersistenciaException("Erro ao salvar configurações em arquivo binário.", erroEscrita);
        }
    }

    /**
     * Lê e desserializa o arquivo binário contendo a {@link Configuracao} global do jogo.
     *
     * @return Instância recuperada de {@link Configuracao}.
     * @throws PersistenciaException Se o arquivo não existir, estiver vazio ou corrompido.
     */
    public Configuracao carregarConfigBinario() {
        File arquivo = new File(this.caminhoConfigBinario);
        if (!arquivo.exists() || !arquivo.isFile()) {
            throw new PersistenciaException("Arquivo binário de configuração não encontrado: " + arquivo.getPath());
        }
        if (arquivo.length() == 0) {
            throw new PersistenciaException("O arquivo binário de configuração está vazio.");
        }

        try (ObjectInputStream leitor = new ObjectInputStream(new FileInputStream(arquivo))) {
            Object objetoLido = leitor.readObject();
            if (!(objetoLido instanceof Configuracao)) {
                throw new PersistenciaException("O arquivo binário não contém um objeto Configuracao válido.");
            }
            return (Configuracao) objetoLido;
        } catch (PersistenciaException erroPersistencia) {
            throw erroPersistencia;
        } catch (IOException | ClassNotFoundException erroLeitura) {
            throw new PersistenciaException("Erro ao carregar configurações do arquivo binário.", erroLeitura);
        }
    }

    /**
     * Serializa um componente polimórfico (Carroceria, Motor, Roda ou Suspensao) em um elemento XML.
     */
    private Element criarElementoComponente(Document doc, String nomeTag, Componente comp, boolean noInventario) {
        Element elem = doc.createElement(nomeTag);
        elem.setAttribute("classe", comp.getClass().getSimpleName());
        elem.setAttribute("noInventario", String.valueOf(noInventario));

        adicionarElementoTexto(doc, elem, "nome", comp.getNome());
        adicionarElementoTexto(doc, elem, "custo", String.valueOf(comp.getCusto()));
        adicionarElementoTexto(doc, elem, "nivelMinimo", String.valueOf(comp.getNivelMinimo()));
        adicionarElementoTexto(doc, elem, "nivelMelhoria", String.valueOf(comp.getNivelMelhoria()));
        adicionarElementoTexto(doc, elem, "peso", String.valueOf(comp.getPeso()));
        adicionarElementoTexto(doc, elem, "durabilidade", String.valueOf(comp.getDurabilidade()));

        if (comp instanceof Carroceria) {
            Carroceria c = (Carroceria) comp;
            adicionarElementoTexto(doc, elem, "modeloBrasileiro", c.getModeloBrasileiro());
        } else if (comp instanceof Motor) {
            Motor m = (Motor) comp;
            adicionarElementoTexto(doc, elem, "potenciaCv", String.valueOf(m.getPotenciaCv()));
        } else if (comp instanceof Roda) {
            Roda r = (Roda) comp;
            adicionarElementoTexto(doc, elem, "tipoPneu", r.getTipoPneu());
            adicionarElementoTexto(doc, elem, "aro", String.valueOf(r.getAro()));
        } else if (comp instanceof Suspensao) {
            Suspensao s = (Suspensao) comp;
            adicionarElementoTexto(doc, elem, "rigidez", String.valueOf(s.getRigidez()));
        } else {
            throw new PersistenciaException("Subtipo de componente não suportado na persistência: "
                    + comp.getClass().getSimpleName());
        }

        return elem;
    }

    /**
     * Reconstrói uma instância concreta de {@link Componente} a partir do seu elemento XML,
     * reaplicando o nível de melhoria e o desgaste de durabilidade salvos.
     */
    private Componente reconstruirComponente(Element elem) {
        String classe = obterAtributoObrigatorio(elem, "classe");
        String nome = obterTextoObrigatorio(elem, "nome");
        int custo = Integer.parseInt(obterTextoObrigatorio(elem, "custo"));
        int nivelMinimo = Integer.parseInt(obterTextoObrigatorio(elem, "nivelMinimo"));
        int nivelMelhoria = Integer.parseInt(obterTextoObrigatorio(elem, "nivelMelhoria"));
        double peso = Double.parseDouble(obterTextoObrigatorio(elem, "peso"));
        double durabilidadeSalva = Double.parseDouble(obterTextoObrigatorio(elem, "durabilidade"));

        if (nivelMelhoria < 0 || durabilidadeSalva < 0.0) {
            throw new PersistenciaException("Atributos de componente inválidos no XML para '" + nome + "'.");
        }

        Componente componente;
        switch (classe) {
            case "Carroceria":
                String modelo = obterTextoObrigatorio(elem, "modeloBrasileiro");
                componente = new Carroceria(nome, modelo, custo, nivelMinimo, peso);
                break;
            case "Motor":
                int potenciaCv = Integer.parseInt(obterTextoObrigatorio(elem, "potenciaCv"));
                componente = new Motor(nome, potenciaCv, custo, nivelMinimo, peso);
                break;
            case "Roda":
                String tipoPneu = obterTextoObrigatorio(elem, "tipoPneu");
                int aro = Integer.parseInt(obterTextoObrigatorio(elem, "aro"));
                componente = new Roda(nome, tipoPneu, aro, custo, nivelMinimo);
                break;
            case "Suspensao":
                double rigidez = Double.parseDouble(obterTextoObrigatorio(elem, "rigidez"));
                componente = new Suspensao(nome, rigidez, custo, nivelMinimo);
                break;
            default:
                throw new PersistenciaException("Tipo de componente desconhecido no XML: " + classe);
        }

        for (int i = 0; i < nivelMelhoria; i++) {
            componente.aplicarMelhoria();
        }

        double desgaste = componente.getDurabilidade() - durabilidadeSalva;
        if (desgaste > 0.0001) {
            componente.desgastar(desgaste);
        }

        return componente;
    }

    /**
     * Remove caracteres inválidos para nome de arquivo ao salvar ou buscar perfis.
     */
    private String sanitizarNomeArquivo(String nomePerfil) {
        String limpo = nomePerfil.trim().replaceAll("[^a-zA-Z0-9_\\- ]", "");
        if (limpo.isEmpty()) {
            throw new PersistenciaException("Nome de perfil inválido para geração de arquivo: " + nomePerfil);
        }
        return limpo;
    }

    /**
     * Cria e anexa uma tag XML filha contendo texto simples.
     */
    private void adicionarElementoTexto(Document doc, Element pai, String nomeTag, String valor) {
        Element elem = doc.createElement(nomeTag);
        elem.setTextContent(valor);
        pai.appendChild(elem);
    }

    /**
     * Lê um atributo obrigatório de um elemento XML ou lança {@link PersistenciaException}.
     */
    private String obterAtributoObrigatorio(Element elem, String nomeAtributo) {
        String valor = elem.getAttribute(nomeAtributo);
        if (valor == null || valor.trim().isEmpty()) {
            throw new PersistenciaException(
                    "Atributo obrigatório '" + nomeAtributo + "' ausente em <" + elem.getTagName() + ">.");
        }
        return valor.trim();
    }

    /**
     * Obtém o primeiro elemento filho obrigatório com a tag especificada.
     */
    private Element obterElementoFilhoObrigatorio(Element pai, String nomeTag) {
        NodeList nodes = pai.getElementsByTagName(nomeTag);
        if (nodes.getLength() == 0) {
            throw new PersistenciaException("Elemento obrigatório <" + nomeTag + "> ausente no XML.");
        }
        return (Element) nodes.item(0);
    }

    /**
     * Extrai o conteúdo textual obrigatório de uma tag filha ou lança {@link PersistenciaException}.
     */
    private String obterTextoObrigatorio(Element pai, String nomeTag) {
        Element elem = obterElementoFilhoObrigatorio(pai, nomeTag);
        String texto = elem.getTextContent();
        if (texto == null || texto.trim().isEmpty()) {
            throw new PersistenciaException("Elemento <" + nomeTag + "> não pode estar vazio no XML.");
        }
        return texto.trim();
    }
}