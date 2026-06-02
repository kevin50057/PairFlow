import { Component, Input, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Location } from '@angular/common';
import { Router } from '@angular/router';
import { Api } from '../../core/api';
import { Auth } from '../../core/auth';
import { Todo } from '../../core/models';
import { ASSIGNEE, PRIORITY, TODO_TYPE } from '../../core/labels';

@Component({
  selector: 'pf-todo-detail',
  imports: [FormsModule],
  template: `
    <div class="appbar"><button class="back" (click)="loc.back()">‹</button><h1 style="font-size:1.1rem">任務詳情</h1><span></span></div>
    @if (todo(); as t) {
      <div class="screen stack">
        <div class="card">
          <h2 [class.strike]="t.status === 'DONE'">{{ t.title }}</h2>
          <div class="row wrap">
            <span class="tag">{{ type(t.type) }}</span>
            <span class="tag">優先 {{ prio(t.priority) }}</span>
            <span class="tag">負責 {{ who(t.assignee) }}</span>
            @if (t.dueDate) { <span class="tag">截止 {{ date(t.dueDate) }}</span> }
            @if (t.isSecret) { <span class="badge badge-soft">驚喜任務</span> }
          </div>
          @if (t.description) { <p class="small" style="margin-top:10px">{{ t.description }}</p> }
          @if (t.type === 'GOAL' && t.goalTarget) {
            <div class="notice" style="margin-top:10px">目前進度：{{ t.goalCurrent || 0 }} / {{ t.goalTarget }} {{ t.goalUnit }}</div>
          }
        </div>

        <div class="card">
          <div class="section-title" style="margin-top:0">子任務</div>
          @if (t.checklist.length) {
            @for (it of t.checklist; track it.id) {
              <div class="row" style="margin:6px 0">
                <span class="todo-check" [class.done]="it.completed" (click)="toggleItem(it)">@if (it.completed) { ✓ }</span>
                <span class="grow" [class.strike]="it.completed">{{ it.title }}</span>
              </div>
            }
          } @else { <p class="muted small">還沒有子任務</p> }
          <div class="row" style="margin-top:8px">
            <input class="input" placeholder="新增子任務…" name="ni" [(ngModel)]="newItem" />
            <button class="btn btn-ghost btn-sm" (click)="addItem()">加入</button>
          </div>
        </div>

        <div class="card">
          <div class="section-title" style="margin-top:0">留言</div>
          @for (c of t.comments || []; track c.id) {
            <div style="margin:6px 0">
              <b class="small">{{ c.authorId === me() ? '我' : '對方' }}</b>
              <span class="small">：{{ c.content }}</span>
            </div>
          }
          @if (!(t.comments?.length)) { <p class="muted small">還沒有留言</p> }
          <div class="row" style="margin-top:8px">
            <input class="input" placeholder="留個言…" name="nc" [(ngModel)]="newComment" />
            <button class="btn btn-ghost btn-sm" (click)="addComment()">送出</button>
          </div>
        </div>

        <div class="row">
          @if (t.status !== 'DONE') { <button class="btn btn-primary grow" (click)="complete()">完成任務</button> }
          @else { <button class="btn btn-outline grow" (click)="reopen()">重新開啟</button> }
          <button class="btn btn-outline" (click)="remove()" style="color:var(--danger)">刪除</button>
        </div>
      </div>
    } @else { <div class="empty">載入中…</div> }
  `,
})
export class TodoDetailPage implements OnInit {
  @Input() id = '';
  private api = inject(Api);
  private auth = inject(Auth);
  loc = inject(Location);
  private router = inject(Router);

  todo = signal<Todo | null>(null);
  newItem = '';
  newComment = '';

  ngOnInit() { this.load(); }

  async load() {
    try { this.todo.set(await this.api.get<Todo>('/todos/' + this.id)); }
    catch { this.router.navigateByUrl('/todos'); }
  }

  me() { return this.auth.user()?.id; }
  type(c: string) { return TODO_TYPE[c] ?? c; }
  prio(c: string) { return PRIORITY[c] ?? c; }
  who(c: string) { return ASSIGNEE[c] ?? c; }
  date(iso: string) { return new Date(iso).toLocaleString('zh-TW', { month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit', hour12: false }); }

  async complete() { await this.api.post(`/todos/${this.id}/complete`); this.load(); }
  async reopen() { await this.api.patch(`/todos/${this.id}`, { status: 'PENDING' }); this.load(); }
  async toggleItem(it: { id: string; completed: boolean }) { await this.api.patch(`/todos/${this.id}/checklist/${it.id}`, { isCompleted: !it.completed }); this.load(); }
  async addItem() { if (!this.newItem.trim()) return; await this.api.post(`/todos/${this.id}/checklist`, { title: this.newItem.trim() }); this.newItem = ''; this.load(); }
  async addComment() { if (!this.newComment.trim()) return; await this.api.post(`/todos/${this.id}/comments`, { content: this.newComment.trim() }); this.newComment = ''; this.load(); }
  async remove() { await this.api.del('/todos/' + this.id); this.router.navigateByUrl('/todos'); }
}
