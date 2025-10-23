# QNOTA

Monorepo do **QNOTA** — plataforma acadêmica organizada em módulos (Domínio, Aplicação, Infraestrutura e Camadas de Apresentação).  
O repositório é **multi-módulo Maven** e usa **BDD (Cucumber)** como documentação viva dos requisitos.

> ### 📚 Onde está a documentação?
>

> - **Cenários Gherkin (Living Documentation):**  
>   `./dominio-principal/src/test/resources/dev/com/qnota/dominio/principal/*.feature`
> - **StoryMap e Descrição detalhada do domínio (versão "em texto" logo abaixo):**  
>   `./Documentação/StoryMap-Qnota.pdf` e `./Documentação/Descricao-Detalhada-Qnota.pdf`
=======
> - **StoryMap e Descrição detalhada do domínio (versão “em texto” logo abaixo):**
>   `Eles estão também no diretório "Documentacao" do projeto
>   `[Descricao-Detalhada-Qnota.pdf](https://github.com/user-attachments/files/23062028/Descricao-Detalhada-Qnota.pdf)` e `Storymap aqui!.pdf`
> - **Context Mapper (limites/contextos):**  
>   `./qnota.cml`


## Protótipo

- Link: https://www.figma.com/make/wx4obguhFd80NeadgO1Knm/Naveg%C3%A1vel-QNota-para-Coordenador?node-id=0-1&p=f&t=vP0KSof6mrg4P5Wd-0&fullscreen=1

---

## Apresentação

- Pdf: [Apresentação Qnota.pdf](https://github.com/user-attachments/files/23062053/Apresentacao.Qnota.pdf)


## Como buildar

Build do monorepo:

```bash
mvn -T1C clean verify
# ou
mvn test
