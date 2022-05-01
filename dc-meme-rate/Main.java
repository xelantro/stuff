package de.memium.memerater;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;

public class Main {

	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		final String token = "";
		final DiscordClient client = DiscordClient.create(token);
		final GatewayDiscordClient gateway = client.login().block();

		//gateway.getChannelById(Snowflake.of(1L)).block()#
		//gateway.getMessageById(Snowflake.of(820688968718614538L), Snowflake.of(945822961808592947L)).block().addReaction(ReactionEmoji.unicode("\ud83d\udc4d")).block();

		gateway.on(MessageCreateEvent.class).subscribe(event -> {
			final Message message = event.getMessage();
			if (message.getContent().startsWith("!memerate")) {
				String linkprefix = "https://discord.com/channels/"
						+((message.getGuildId().isPresent())?message.getGuildId().get().asLong():"@me")
						+"/"+message.getChannelId().asLong()+"/";

				int count = 0;
				try {
					count = Integer.parseInt(message.getContent().split(" ")[1]);
				} catch(Exception e) {
					count = 10;
				}


				ArrayList<String> emotes = new ArrayList<String>();
				//boolean unicode = Pattern.compile("\\p{So}").matcher(message.getContent()).find();
				Matcher match = Pattern.compile(":([a-z0-9]+):|(\\p{So}+)").matcher(message.getContent().toLowerCase()); //java only for non java use ":([a-zA-Z0-9]+):|\s(\p{So}|[\x{1F1E6}-\x{1F1FF}]{2})\s"
				while (match.find()) {
					emotes.add(match.group(match.group().startsWith(":")?1:2));
				}
				boolean allemotes = emotes.size()==0; //message.getContent().contains("-all");

				final MessageChannel channel = message.getChannel().block();

				Message o = channel.createMessage("Running: This may take a while").block();
				//System.out.println("Msg: "+message.getContent());
				//channel.createMessage("Emotes: "+emotes.stream().collect(Collectors.joining(", "))).block();
				//System.out.println("emotes: "+emotes.stream().collect(Collectors.joining(", ")));

				List<Funnymeme> l = channel.getMessagesBefore(message.getId())
						.filter(m -> m.getReactions().size() > 0)
						.map(m -> new Funnymeme(
								m.getReactions().stream()
								.filter(r -> allemotes ||
										r.getEmoji().asUnicodeEmoji().filter(u -> emotes.contains(u.getRaw())).isPresent() ||
										r.getEmoji().asEmojiData().name().filter(e -> emotes.contains(e.toLowerCase())).isPresent())
								.map(r -> r.getCount()).reduce(new Integer(0), (oI, nI) -> oI+nI),
								m.getId().asLong()
								))
						.sort((m1, m2) -> m2.xds-m1.xds)
						.collectList().block();
				l.removeIf(m -> m.xds==0);
				if(count>l.size()) count = l.size();

				String out = "Messages with the most reactions:\n";
				for(int i = 0; i<= count-1; i++) {
					out += (i+1)+". "+l.get(i).xds+" reactions: "+linkprefix+l.get(i).msgid+"\n";
				}

				o.edit().withContent(out).block();
			}
		});

		gateway.onDisconnect().block();
	}

	static class Funnymeme {
		public int xds;
		public long msgid;
		public Funnymeme(int xds, long msgid) {
			this.xds = xds;
			this.msgid = msgid;
		}
	}
}
