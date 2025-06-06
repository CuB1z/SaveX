import { Component, Input } from "@angular/core";
import { Post } from "@/types/Posts";
import { environment } from "@environments/environment";

@Component({
	selector: "app-post-card",
	templateUrl: "./post-card.component.html",
	styleUrls: ["./post-card.component.css"]
})
export class PostCardComponent {
	@Input() post!: Post;
	@Input() extendedClass: string = "";

	getBannerUrl(): string {
		return `${environment.baseApiUrl}/v1/posts/${this.post.id}/banner`;
	}
}