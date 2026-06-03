import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditorMensagem } from './editor-mensagem';

describe('EditorMensagem', () => {
  let component: EditorMensagem;
  let fixture: ComponentFixture<EditorMensagem>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EditorMensagem],
    }).compileComponents();

    fixture = TestBed.createComponent(EditorMensagem);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
