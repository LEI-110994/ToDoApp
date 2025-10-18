package com.example.todoapp.views;

import com.example.examplefeature.Task;
import com.example.examplefeature.TaskService;
import com.example.todoapp.util.QRCodeService;
import com.google.zxing.WriterException;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Base64;
import java.util.Optional;

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

@Route("qrcode-tasks")
@PageTitle("QR Code de Tarefas")
@Menu(order = 2, icon = "vaadin:qrcode", title = "QR Code de Tarefas")
public class QRCodeView extends VerticalLayout {

    private final TaskService taskService;
    private final QRCodeService qrCodeService;
    private final Grid<Task> taskGrid;

    public QRCodeView(TaskService taskService) {
        this.taskService = taskService;
        this.qrCodeService = new QRCodeService();

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Título
        H2 title = new H2("Gerar QR Code de Tarefas");
        add(title);

        // Formatadores de data
        var dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withLocale(getLocale())
                .withZone(ZoneId.systemDefault());
        var dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                .withLocale(getLocale());

        // Grid de tarefas
        taskGrid = new Grid<>();
        taskGrid.setItems(query -> taskService.list(toSpringPageRequest(query)).stream());
        taskGrid.addColumn(Task::getDescription).setHeader("Description").setFlexGrow(3);
        taskGrid.addColumn(task -> Optional.ofNullable(task.getDueDate())
                .map(dateFormatter::format)
                .orElse("Never"))
                .setHeader("Due Date").setFlexGrow(1);
        taskGrid.addColumn(task -> dateTimeFormatter.format(task.getCreationDate()))
                .setHeader("Creation Date").setFlexGrow(1);
        taskGrid.addComponentColumn(this::createQRCodeButton)
                .setHeader("QR Code")
                .setWidth("150px")
                .setFlexGrow(0);
        taskGrid.setSizeFull();

        add(taskGrid);
    }

    private Button createQRCodeButton(Task task) {
        Button qrButton = new Button("Gerar QR Code");
        qrButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        qrButton.addClickListener(event -> generateAndOpenQRCode(task));
        return qrButton;
    }

    private void generateAndOpenQRCode(Task task) {
        try {
            // Gera o texto com as informações da tarefa (para o QR Code)
            String taskInfo = formatTaskInfo(task);

            // Gera a versão HTML (para exibição na página)
            String taskInfoHtml = formatTaskInfoHtml(task);

            // Gera o QR Code como array de bytes (500x500 para boa qualidade)
            byte[] qrCodeBytes = qrCodeService.generateQRCodeToByteArray(taskInfo, 500, 500);

            // Converte para Base64
            String base64Image = Base64.getEncoder().encodeToString(qrCodeBytes);
            String dataUrl = "data:image/png;base64," + base64Image;

            // Converte o ID para String para evitar erro de serialização JSON
            String taskIdStr = String.valueOf(task.getId());

            // Abre em uma nova aba usando JavaScript
            getUI().ifPresent(ui -> {
                ui.getPage().executeJs(
                    "var newWindow = window.open('', '_blank');" +
                    "if (newWindow) {" +
                    "  newWindow.document.open();" +
                    "  newWindow.document.write(" +
                    "    '<html><head>' +" +
                    "    '<title>QR Code - Task #' + $0 + '</title>' +" +
                    "    '<meta charset=\"UTF-8\">' +" +
                    "    '<style>' +" +
                    "    'body{margin:0;display:flex;justify-content:center;align-items:center;' +" +
                    "    'min-height:100vh;background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);' +" +
                    "    'font-family:-apple-system,BlinkMacSystemFont,\"Segoe UI\",Roboto,sans-serif;flex-direction:column;}' +" +
                    "    'img{max-width:90%;height:auto;border:3px solid white;border-radius:12px;' +" +
                    "    'background:white;padding:30px;box-shadow:0 20px 60px rgba(0,0,0,0.3);}' +" +
                    "    'h2{color:white;margin-bottom:30px;font-size:2em;text-shadow:0 2px 10px rgba(0,0,0,0.3);}' +" +
                    "    '.info-box{background:white;padding:25px;border-radius:12px;border:none;' +" +
                    "    'max-width:500px;margin-top:30px;' +" +
                    "    'box-shadow:0 10px 30px rgba(0,0,0,0.2);font-size:14px;line-height:1.8;}' +" +
                    "    '.info-box h3{margin-top:0;color:#667eea;border-bottom:2px solid #667eea;padding-bottom:10px;}' +" +
                    "    '.info-item{margin:15px 0;padding:10px;background:#f8f9fa;border-radius:6px;}' +" +
                    "    '.info-label{font-weight:bold;color:#333;display:block;margin-bottom:5px;}' +" +
                    "    '.info-value{color:#666;margin-left:10px;}' +" +
                    "    '</style></head><body>' +" +
                    "    '<h2>QR Code da Tarefa</h2>' +" +
                    "    '<img src=\"' + $1 + '\" alt=\"QR Code\"/>' +" +
                    "    '<div class=\"info-box\">' + $2 + '</div>' +" +
                    "    '</body></html>'" +
                    "  );" +
                    "  newWindow.document.close();" +
                    "} else {" +
                    "  alert('Por favor, permita pop-ups para abrir o QR Code em uma nova aba.');" +
                    "}",
                    taskIdStr,
                    dataUrl,
                    taskInfoHtml
                );
            });

            showSuccessNotification("QR Code gerado! Abrindo em nova aba...");

        } catch (WriterException e) {
            showErrorNotification("Erro ao gerar o QR Code: " + e.getMessage());
        } catch (IOException e) {
            showErrorNotification("Erro ao processar a imagem do QR Code: " + e.getMessage());
        } catch (Exception e) {
            showErrorNotification("Erro inesperado: " + e.getMessage());
        }
    }

    private String formatTaskInfo(Task task) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withLocale(getLocale())
            .withZone(ZoneId.systemDefault());
        DateTimeFormatter dateFormatter = DateTimeFormatter
            .ofLocalizedDate(FormatStyle.MEDIUM)
            .withLocale(getLocale());

        StringBuilder info = new StringBuilder();
        info.append("═══════════════════════════════════\n");
        info.append("       INFORMAÇÕES DA TAREFA       \n");
        info.append("═══════════════════════════════════\n\n");

        info.append("Descrição:\n");
        info.append("   ").append(task.getDescription()).append("\n\n");

        info.append("ID da Tarefa: ").append(task.getId()).append("\n\n");

        info.append("Data de Criação:\n");
        info.append("   ").append(dateTimeFormatter.format(task.getCreationDate())).append("\n\n");

        if (task.getDueDate() != null) {
            info.append("Prazo de Conclusão:\n");
            info.append("   ").append(dateFormatter.format(task.getDueDate())).append("\n");
        } else {
            info.append("Prazo: Sem prazo definido\n");
        }

        info.append("\n═══════════════════════════════════");

        return info.toString();
    }

    private String formatTaskInfoHtml(Task task) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withLocale(getLocale())
            .withZone(ZoneId.systemDefault());
        DateTimeFormatter dateFormatter = DateTimeFormatter
            .ofLocalizedDate(FormatStyle.MEDIUM)
            .withLocale(getLocale());

        StringBuilder html = new StringBuilder();
        html.append("<h3>Informações da Tarefa</h3>");
        
        html.append("<div class=\"info-item\">");
        html.append("<span class=\"info-label\">Descrição:</span>");
        html.append("<span class=\"info-value\">").append(escapeHtml(task.getDescription())).append("</span>");
        html.append("</div>");

        html.append("<div class=\"info-item\">");
        html.append("<span class=\"info-label\">ID da Tarefa:</span>");
        html.append("<span class=\"info-value\">").append(task.getId()).append("</span>");
        html.append("</div>");

        html.append("<div class=\"info-item\">");
        html.append("<span class=\"info-label\">Data de Criação:</span>");
        html.append("<span class=\"info-value\">").append(dateTimeFormatter.format(task.getCreationDate())).append("</span>");
        html.append("</div>");

        html.append("<div class=\"info-item\">");
        html.append("<span class=\"info-label\">Prazo de Conclusão:</span>");
        if (task.getDueDate() != null) {
            html.append("<span class=\"info-value\">").append(dateFormatter.format(task.getDueDate())).append("</span>");
        } else {
            html.append("<span class=\"info-value\" style=\"color:#999;\">Sem prazo definido</span>");
        }
        html.append("</div>");

        return html.toString();
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    private void showSuccessNotification(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showErrorNotification(String message) {
        Notification notification = Notification.show(message, 5000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
