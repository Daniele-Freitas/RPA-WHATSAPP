import { Component } from '@angular/core';
import { FileUploadComponent } from '../../shared/components/file-upload/file-upload';


@Component({
  selector: 'app-criacao-campanha',
  imports: [FileUploadComponent],
  templateUrl: './criacao-campanha.html',
  styleUrl: './criacao-campanha.scss',
  standalone: true
})
export class CriacaoCampanha {}
