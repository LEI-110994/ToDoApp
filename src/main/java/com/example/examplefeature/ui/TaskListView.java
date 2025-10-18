package com.example.examplefeature.ui;

import com.example.base.ui.component.ViewToolbar;
import com.example.examplefeature.Task;
import com.example.examplefeature.TaskService;
import com.example.todoapp.util.EmailService;
import com.example.todoapp.util.PdfService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

@Route("")
@PageTitle("Task List")
@Menu(order = 0, icon = "vaadin:clipboard-check", title = "Task List")
class TaskListView extends Main {

    private final TaskService taskService;

    final TextField description;
    final DatePicker dueDate;
    final Button createBtn;
    final Grid<Task> taskGrid;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PdfService pdfService;

    TaskListView(TaskService taskService) {
        this.taskService = taskService;

        // ========================
        // Secção 1: Tarefas (original)
        // ========================

        description = new TextField();
        description.setPlaceholder("What do you want to do?");
        description.setAriaLabel("Task description");
        description.setMaxLength(Task.DESCRIPTION_MAX_LENGTH);
        description.setMinWidth("20em");

        dueDate = new DatePicker();
        dueDate.setPlaceholder("Due date");
        dueDate.setAriaLabel("Due date");

        createBtn = new Button("Create", event -> createTask());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Botão para exportar PDF
        Button exportPdfBtn = new Button("Exportar PDF", event -> exportToPdf());
        exportPdfBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        var dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(getLocale())
                .withZone(ZoneId.systemDefault());
        var dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(getLocale());

        taskGrid = new Grid<>();
        taskGrid.setItems(query -> taskService.list(toSpringPageRequest(query)).stream());
        taskGrid.addColumn(Task::getDescription).setHeader("Description");
        taskGrid.addColumn(task -> Optional.ofNullable(task.getDueDate()).map(dateFormatter::format).orElse("Never"))
                .setHeader("Due Date");
        taskGrid.addColumn(task -> dateTimeFormatter.format(task.getCreationDate())).setHeader("Creation Date");
        taskGrid.setSizeFull();

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);

        add(new ViewToolbar("Task List", ViewToolbar.group(description, dueDate, createBtn, exportPdfBtn)));
        add(taskGrid);

        // ========================
        // Secção 2: Envio de Emails (importada da EmailView)
        // ========================

        H2 emailHeader = new H2("Enviar Email");
        TextField destinatario = new TextField("Destinatário");
        TextField assunto = new TextField("Assunto");
        TextArea corpo = new TextArea("Mensagem");
        corpo.setWidthFull();

        Button enviar = new Button("Enviar Email", e -> {
            try {
                emailService.sendSimpleEmail(
                        destinatario.getValue(),
                        assunto.getValue(),
                        corpo.getValue()
                );
                Notification.show("Email enviado com sucesso!", 4000, Notification.Position.MIDDLE);
            } catch (Exception ex) {
                Notification.show("Erro ao enviar email: " + ex.getMessage(),
                        5000, Notification.Position.MIDDLE);
            }
        });
        enviar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        VerticalLayout emailLayout = new VerticalLayout(emailHeader, destinatario, assunto, corpo, enviar);
        emailLayout.addClassNames(LumoUtility.Margin.Top.LARGE);
        emailLayout.setWidthFull();

        add(emailLayout);
    }

    private void createTask() {
        taskService.createTask(description.getValue(), dueDate.getValue());
        taskGrid.getDataProvider().refreshAll();
        description.clear();
        dueDate.clear();
        Notification.show("Task added", 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }


    private void exportToPdf() {
        try {
            // Obter todas as tarefas
            List<Task> tasks = taskService.findAll();

            if (tasks.isEmpty()) {
                Notification.show("Não há tarefas para exportar!", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_WARNING);
                return;
            }

            // Gerar PDF
            ByteArrayOutputStream pdfStream = pdfService.generateTaskListPdf(tasks);
            byte[] pdfBytes = pdfStream.toByteArray();

            // Converter para Base64
            String base64Pdf = Base64.getEncoder().encodeToString(pdfBytes);

            // Criar Blob e abrir em nova aba usando JavaScript
            getElement().executeJs(
                    "const byteCharacters = atob($0);" +
                            "const byteNumbers = new Array(byteCharacters.length);" +
                            "for (let i = 0; i < byteCharacters.length; i++) {" +
                            "    byteNumbers[i] = byteCharacters.charCodeAt(i);" +
                            "}" +
                            "const byteArray = new Uint8Array(byteNumbers);" +
                            "const blob = new Blob([byteArray], {type: 'application/pdf'});" +
                            "const url = URL.createObjectURL(blob);" +
                            "window.open(url, '_blank');" +
                            "setTimeout(() => URL.revokeObjectURL(url), 100);",
                    base64Pdf
            );

            Notification.show("PDF gerado com sucesso!", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        } catch (Exception e) {
            Notification.show("Erro ao gerar PDF: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            e.printStackTrace();
        }
    }

}