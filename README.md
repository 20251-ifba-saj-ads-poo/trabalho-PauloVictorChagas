# Reavaliação de Relatório - trabalho-PauloVictorChagas

## Descrição do Trabalho

Sistema de gestão financeira pessoal (fluxo de caixa simplificado)

Desenvolva um sistema para gerenciar finanças pessoais. O sistema deve permitir o registro de despesas fixas mensais, gastos eventuais e receitas. O usuário deve poder consultar o saldo atual, prever o saldo futuro e gerar relatórios mensais e anuais por categoria.

## Resumo da Pontuação (Reavaliação Crítica)

| Critério | Pontuação Máxima | Pontuação Obtida | Comentários |
| --- | --- | --- | --- |
| **Interface Gráfica (JavaFX)** | **20** | **20** | A interface permanece funcional, completa e bem implementada. |
| **Camada de Negócio** | **30** | **10** | Falha grave. A lógica de validação está na camada de controle, e não foram criadas exceções de negócio, contrariando o Modelo. |
| **Camada de Dados** | **20** | **20** | A reescrita da implementação do `Repository` é provavelmente feita por IA. |
| **Separação em Camadas** | **20** | **10** | A separação de responsabilidades foi violada, com a camada de controle assumindo a lógica de validação que pertence à camada de serviço. |
| **Boas Práticas** | **10** | **6** | O aluno falhou em seguir a arquitetura básica solicitada, que é um requisito primário. |
| **Total** | **100** | **66** |  |

---

### Pontos Fortes (Mantidos)

- **Qualidade Técnica:** O código continua sendo de alta qualidade. O uso de `BigDecimal` para finanças, a implementação de um `EventBus` para desacoplamento e a criação de um `Repository` robusto e otimizado são pontos que demonstram grande habilidade técnica.

### Falhas Críticas na Aderência ao Modelo

#### 1. [GRAVE] Lógica de Negócio na Camada Errada
O Modelo é explícito: "A Camada de Negócio aplica as regras e validações". O aluno implementou toda a lógica de validação de dados (campos em branco, valores numéricos) diretamente nos **Controllers** (camada de apresentação), deixando as classes de **Serviço** praticamente vazias.

**Código do Aluno (`CadGastoController.java`):**
```java
// Validação na camada de Apresentação (Controller) - INCORRETO
if (categoria == null || valorStr == null || valorStr.isBlank() || data == null || tipo == null) {
    new Alert(AlertType.WARNING, "Preencha todos os campos antes de salvar.").showAndWait();
    return;
}
//...
try {
    //...
} catch (NumberFormatException e) {
    new Alert(AlertType.ERROR, "Valor inválido! Digite um número válido.").showAndWait();
}
```

**Impacto:** Esta é a falha mais grave do projeto. Ela viola o princípio da separação de camadas. Se outra parte do sistema (ou um futuro aplicativo móvel, por exemplo) precisasse criar um gasto, toda a lógica de validação teria que ser duplicada, pois ela não está centralizada na camada de serviço, que é seu lugar correto.

Chamada ao `clearCache` pelo serviço e pelo controller. 

#### 2. [GRAVE] Ausência de Exceções de Negócio Personalizadas
O `Modelo.md` instrui: "As exceções devem ser corretamente lançadas pela camada de negócio [...] As exceções devem ser personalizadas". O aluno não criou exceções como `ValorGastoInvalidoException` ou `CampoObrigatorioException`. Em vez disso, ele lança `Alerts` diretamente do controller.

**Impacto:** A camada de serviço não comunica erros de negócio de forma padronizada. O tratamento de erros fica acoplado à tecnologia da interface (JavaFX `Alert`), dificultando o reuso da camada de serviço em outros contextos.

#### 3. Camada de Serviço Subutilizada
Como consequência dos pontos anteriores, as classes `GastoService` e `ReceitaService` não contêm lógica de negócio e não sobrescrevem os métodos `create` ou `update` para adicionar validação, como era esperado. Elas são meros repassadores de chamadas para o repositório.
