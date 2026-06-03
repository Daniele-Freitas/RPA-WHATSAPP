import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CriacaoCampanha } from './criacao-campanha';

describe('CriacaoCampanha', () => {
  let component: CriacaoCampanha;
  let fixture: ComponentFixture<CriacaoCampanha>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CriacaoCampanha],
    }).compileComponents();

    fixture = TestBed.createComponent(CriacaoCampanha);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
