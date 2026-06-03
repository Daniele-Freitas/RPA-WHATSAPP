import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MapeamentoColunas } from './mapeamento-colunas';

describe('MapeamentoColunas', () => {
  let component: MapeamentoColunas;
  let fixture: ComponentFixture<MapeamentoColunas>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MapeamentoColunas],
    }).compileComponents();

    fixture = TestBed.createComponent(MapeamentoColunas);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
