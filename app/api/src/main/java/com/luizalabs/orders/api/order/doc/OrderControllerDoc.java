package com.luizalabs.orders.api.order.doc;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

import com.luizalabs.orders.domain.dto.ProcessingResult;
import com.luizalabs.orders.domain.dto.UserOrderResponseDTO;
import com.luizalabs.orders.domain.enums.ProcessingStatus;
import com.luizalabs.orders.usecase.port.data.FileProcessingLogPort.LogInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Orders API", description = "API para processamento e consulta de pedidos")
public interface OrderControllerDoc {

    @Operation(
            summary = "Upload de arquivo de pedidos",
            description =
                    """
            Processa um arquivo de texto com formato de largura fixa (95 caracteres por linha).

            **Formato esperado:**
            - user_id (10 chars): ID do usuário
            - name (45 chars): Nome do usuário
            - order_id (10 chars): ID do pedido
            - product_id (10 chars): ID do produto
            - value (12 chars): Valor do produto
            - date (8 chars): Data do pedido (yyyyMMdd)

            **Exemplo de linha:**
            ```
            0000000070                              Palmer Prosacco00000007530000000003     1836.7420210308
            ```

            **Comportamento:**
            - Linhas válidas são processadas e salvas
            - Linhas inválidas são registradas como erros
            - Retorna dados normalizados + log de erros
            """,
            parameters = {
                @Parameter(
                        name = "file",
                        description =
                                "Arquivo .txt com dados de pedidos (formato largura fixa, 95"
                                        + " chars/linha)",
                        required = true,
                        example = "data.txt")
            })
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Arquivo processado com sucesso (sem erros)",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ProcessingResult.class),
                                        examples =
                                                @ExampleObject(
                                                        name = "Sucesso",
                                                        value =
                                                                """
                        {
                          "logId": 1,
                          "status": "SUCCESS",
                          "totalLines": 10,
                          "processedLines": 10,
                          "errorLines": 0,
                          "hasErrors": false,
                          "data": [
                            {
                              "user_id": 70,
                              "name": "Palmer Prosacco",
                              "orders": [
                                {
                                  "order_id": 753,
                                  "total": "1836.74",
                                  "date": "2021-03-08",
                                  "products": [
                                    {
                                      "product_id": 3,
                                      "value": "1836.74"
                                    }
                                  ]
                                }
                              ]
                            }
                          ]
                        }
                        """))),
                @ApiResponse(
                        responseCode = "207",
                        description =
                                "Processamento parcial (algumas linhas com erro, mas dados válidos"
                                        + " foram salvos)",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ProcessingResult.class),
                                        examples =
                                                @ExampleObject(
                                                        name = "Parcial",
                                                        value =
                                                                """
                        {
                          "logId": 2,
                          "status": "PARTIAL_SUCCESS",
                          "totalLines": 10,
                          "processedLines": 8,
                          "errorLines": 2,
                          "hasErrors": true,
                          "data": [...]
                        }
                        """))),
                @ApiResponse(
                        responseCode = "400",
                        description =
                                "Requisição inválida (arquivo vazio, extensão incorreta, etc)",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples =
                                                @ExampleObject(
                                                        name = "Erro de Validação",
                                                        value =
                                                                """
                        {
                          "timestamp": "2025-11-30T12:00:00",
                          "status": 400,
                          "error": "Bad Request",
                          "message": "Arquivo vazio"
                        }
                        """))),
                @ApiResponse(
                        responseCode = "422",
                        description = "Arquivo não processado (todas as linhas com erro)",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ProcessingResult.class),
                                        examples =
                                                @ExampleObject(
                                                        name = "Falha Total",
                                                        value =
                                                                """
                        {
                          "logId": 3,
                          "status": "FAILED",
                          "totalLines": 5,
                          "processedLines": 0,
                          "errorLines": 5,
                          "hasErrors": true,
                          "data": []
                        }
                        """))),
                @ApiResponse(
                        responseCode = "500",
                        description = "Erro interno do servidor",
                        content = @Content(mediaType = "application/json"))
            })
    ResponseEntity<ProcessingResult> upload(MultipartFile file);

    @Operation(
            summary = "Consultar pedidos",
            description =
                    """
            Busca pedidos com filtros opcionais.

            **Filtros disponíveis:**
            - Sem filtros: retorna todos os pedidos
            - `orderId`: filtra por ID específico do pedido
            - `startDate` + `endDate`: filtra por intervalo de datas

            **Nota:** Os filtros `orderId` e `startDate/endDate` são mutuamente exclusivos.
            Se ambos forem fornecidos, `orderId` terá prioridade.
            """,
            parameters = {
                @Parameter(
                        name = "orderId",
                        description = "ID do pedido para filtrar",
                        example = "123",
                        required = false),
                @Parameter(
                        name = "startDate",
                        description = "Data inicial do intervalo (formato: yyyy-MM-dd)",
                        example = "2021-01-01",
                        required = false),
                @Parameter(
                        name = "endDate",
                        description = "Data final do intervalo (formato: yyyy-MM-dd)",
                        example = "2021-12-31",
                        required = false)
            })
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Lista de pedidos encontrados",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema =
                                                @Schema(
                                                        implementation =
                                                                UserOrderResponseDTO.class),
                                        examples =
                                                @ExampleObject(
                                                        name = "Lista de Pedidos",
                                                        value =
                                                                """
                        [
                          {
                            "user_id": 70,
                            "name": "Palmer Prosacco",
                            "orders": [
                              {
                                "order_id": 753,
                                "total": "1836.74",
                                "date": "2021-03-08",
                                "products": [
                                  {
                                    "product_id": 3,
                                    "value": "1836.74"
                                  }
                                ]
                              }
                            ]
                          }
                        ]
                        """))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Parâmetros de filtro inválidos",
                        content = @Content(mediaType = "application/json")),
                @ApiResponse(
                        responseCode = "500",
                        description = "Erro interno do servidor",
                        content = @Content(mediaType = "application/json"))
            })
    ResponseEntity<List<UserOrderResponseDTO>> query(
            Long orderId,
            @DateTimeFormat(iso = DATE) LocalDate startDate,
            @DateTimeFormat(iso = DATE) LocalDate endDate);

    @Operation(
            summary = "Buscar log de processamento por ID",
            description =
                    """
            Retorna os detalhes de um log de processamento específico, incluindo:
            - Nome do arquivo processado
            - Status do processamento (SUCCESS, PARTIAL_SUCCESS, FAILED)
            - Quantidade de linhas processadas e com erro
            - Lista detalhada de erros (se houver)
            - Tempo de processamento em milissegundos
            """,
            parameters = {
                @Parameter(
                        name = "id",
                        description = "ID do log de processamento",
                        example = "1",
                        required = true)
            })
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Log encontrado com sucesso",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples =
                                                @ExampleObject(
                                                        name = "Log de Sucesso",
                                                        value =
                                                                """
                        {
                          "id": 1,
                          "fileName": "data.txt",
                          "uploadedAt": "2025-11-30T12:00:00",
                          "status": "SUCCESS",
                          "totalLines": 10,
                          "processedLines": 10,
                          "errorLines": 0,
                          "errors": [],
                          "processingTimeMs": 1234
                        }
                        """))),
                @ApiResponse(
                        responseCode = "404",
                        description = "Log não encontrado",
                        content = @Content(mediaType = "application/json"))
            })
    ResponseEntity<LogInfo> getLogById(Long id);

    @Operation(
            summary = "Listar logs de processamento",
            description =
                    """
            Retorna uma lista paginada de logs de processamento.

            **Filtros disponíveis:**
            - Sem filtros: retorna todos os logs
            - `status`: filtra por status (SUCCESS, PARTIAL_SUCCESS, FAILED, PROCESSING)

            **Ordenação:** Mais recentes primeiro (uploadedAt DESC)
            """,
            parameters = {
                @Parameter(
                        name = "status",
                        description =
                                "Status para filtrar (SUCCESS, PARTIAL_SUCCESS, FAILED,"
                                        + " PROCESSING)",
                        example = "SUCCESS",
                        required = false),
                @Parameter(
                        name = "page",
                        description = "Número da página (0-indexed)",
                        example = "0",
                        required = false),
                @Parameter(
                        name = "size",
                        description = "Tamanho da página",
                        example = "20",
                        required = false)
            })
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Lista de logs retornada",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples =
                                                @ExampleObject(
                                                        name = "Lista de Logs",
                                                        value =
                                                                """
                        [
                          {
                            "id": 2,
                            "fileName": "orders2.txt",
                            "uploadedAt": "2025-11-30T13:00:00",
                            "status": "PARTIAL_SUCCESS",
                            "totalLines": 5,
                            "processedLines": 4,
                            "errorLines": 1,
                            "errors": [
                              {
                                "lineNumber": 3,
                                "lineContent": "invalid...",
                                "errorMessage": "Linha com tamanho inválido"
                              }
                            ],
                            "processingTimeMs": 567
                          },
                          {
                            "id": 1,
                            "fileName": "orders1.txt",
                            "uploadedAt": "2025-11-30T12:00:00",
                            "status": "SUCCESS",
                            "totalLines": 10,
                            "processedLines": 10,
                            "errorLines": 0,
                            "errors": [],
                            "processingTimeMs": 1234
                          }
                        ]
                        """)))
            })
    ResponseEntity<List<LogInfo>> getLogs(ProcessingStatus status, int page, int size);
}
