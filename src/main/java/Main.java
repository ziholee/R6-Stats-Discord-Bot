import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.events.ReadyEvent; // 올바른 패키지로 수정

import javax.security.auth.login.LoginException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Main extends ListenerAdapter {
    public static void main(String[] args) {
        try {
            String token = "디스코드 봇 토큰을 입력하세요.";
            JDABuilder builder = JDABuilder.createDefault(token);
            builder.setActivity(Activity.playing("Rainbow Six Siege"));
            builder.addEventListeners(new Main());

            // 슬래시 명령어 등록
            builder.addEventListeners(new ListenerAdapter() {
                @Override
                public void onReady(ReadyEvent event) { // 올바른 패키지로 수정
                    event.getJDA().updateCommands().addCommands(
                            Commands.slash("전적", "플레이어의 전적을 조회합니다.")
                                    .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.STRING, "player", "플레이어 이름", true),
                            Commands.slash("도움말", "사용 가능한 명령어를 표시합니다."),
                            Commands.slash("오늘의명언", "오늘의 명언을 알려드립니다.")
                    ).queue();
                }
            });

            builder.build();
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("전적")) {
            String playerName = event.getOption("player").getAsString();
            fetchPlayerStats(playerName, event);
        } else if (event.getName().equals("오늘의 명언")) {
            event.reply("포기하지 않는 자가 승리를 쟁취한다.\n");
        } else if (event.getName().equals("도움말")) {
            event.reply("사용 가능한 명령어:\n" +
                    "/전적 [플레이어 이름] - 플레이어의 전적을 조회합니다.\n" +
                    "/오늘의명언 - 오늘의 명언을 알려드립니다.\n" +
                    "/도움말 - 사용 가능한 명령어를 표시합니다.").queue();
        }
    }

    private void fetchPlayerStats(String playerName, SlashCommandInteractionEvent event) {
        String apiKey = "TRN(전적조회사이트) api키를 받아서 입력하세요.";
        String url = "https://api.tracker.gg/api/v2/r6/standard/profile/pc/" + playerName;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("TRN-Api-Key", apiKey)
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> {
                    event.reply("Player stats: " + response).queue();
                })
                .exceptionally(e -> {
                    event.reply("Error fetching stats: " + e.getMessage()).queue();
                    return null;
                });
    }
}